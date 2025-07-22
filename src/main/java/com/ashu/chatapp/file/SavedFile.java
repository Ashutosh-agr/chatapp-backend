package com.ashu.chatapp.file;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SavedFile {
    private String path; // Local file system path
    private String url;  // Public URL to send to frontend
}
