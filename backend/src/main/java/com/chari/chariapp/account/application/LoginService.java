package com.chari.chariapp.account.application;

import com.chari.chariapp.account.application.port.out.AccessTokenIssuer;
import com.chari.chariapp.account.application.port.out.AccountStore;
import com.chari.chariapp.account.application.port.out.PasswordHasher;
import com.chari.chariapp.account.application.port.out.RefreshSessionStore;
import com.chari.chariapp.account.application.port.out.RefreshTokenGenerator;
import com.chari.chariapp.account.application.port.out.RefreshTokenHasher;
import com.chari.chariapp.account.domain.Account;
import com.chari.chariapp.account.domain.AccountStatus;
import com.chari.chariapp.account.domain.RefreshSession;
import com.chari.chariapp.account.domain.RefreshSessionId;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public class LoginService implements LoginUseCase {

    static final Duration REFRESH_TOKEN_LIFETIME = Duration.ofDays(30);

    private final AccountStore accountStore;
    private final PasswordHasher passwordHasher;
    private final RefreshSessionStore refreshSessionStore;
    private final RefreshTokenGenerator refreshTokenGenerator;
    private final RefreshTokenHasher refreshTokenHasher;
    private final AccessTokenIssuer accessTokenIssuer;
    private final Clock clock;

    public LoginService(
            AccountStore accountStore,
            PasswordHasher passwordHasher,
            RefreshSessionStore refreshSessionStore,
            RefreshTokenGenerator refreshTokenGenerator,
            RefreshTokenHasher refreshTokenHasher,
            AccessTokenIssuer accessTokenIssuer,
            Clock clock
    ) {
        this.accountStore = Objects.requireNonNull(accountStore, "Account store is required");
        this.passwordHasher = Objects.requireNonNull(passwordHasher, "Password hasher is required");
        this.refreshSessionStore = Objects.requireNonNull(refreshSessionStore, "Refresh session store is required");
        this.refreshTokenGenerator = Objects.requireNonNull(refreshTokenGenerator, "Refresh token generator is required");
        this.refreshTokenHasher = Objects.requireNonNull(refreshTokenHasher, "Refresh token hasher is required");
        this.accessTokenIssuer = Objects.requireNonNull(accessTokenIssuer, "Access token issuer is required");
        this.clock = Objects.requireNonNull(clock, "Clock is required");
    }

    @Override
    @Transactional
    public TokenPair login(LoginCommand command) {
        Objects.requireNonNull(command, "Login command is required");
        Account account = (command.nationalIdLookup() != null
                ? accountStore.findByNationalIdLookup(command.nationalIdLookup())
                : accountStore.findByEmailLookup(command.emailLookup()))
                .filter(candidate -> candidate.status() == AccountStatus.ACTIVE)
                .filter(candidate -> passwordHasher.matches(command.rawPassword(), candidate.passwordHash()))
                .orElseThrow(InvalidCredentialsException::new);
        return issueTokenPair(account, Instant.now(clock));
    }

    TokenPair issueTokenPair(Account account, Instant now) {
        String rawRefreshToken = refreshTokenGenerator.generate();
        refreshSessionStore.save(new RefreshSession(
                RefreshSessionId.newId(),
                account.id(),
                refreshTokenHasher.hash(rawRefreshToken),
                now,
                now.plus(REFRESH_TOKEN_LIFETIME),
                null
        ));
        AccessTokenIssuer.IssuedAccessToken accessToken = accessTokenIssuer.issue(account, now);
        return new TokenPair(accessToken.value(), accessToken.expiresAt(), rawRefreshToken);
    }
}
