package com.translation.service;


import java.io.InputStream;

public interface TranslationService {
    String translation(InputStream inputStream, String fileName);
}
