package com.chari.chariapp.additionaldocument.application;

import com.chari.chariapp.additionaldocument.domain.AdditionalDocumentAttachment;
import java.io.InputStream;

public record AdditionalDocumentAttachmentContent(AdditionalDocumentAttachment attachment, InputStream content) { }
