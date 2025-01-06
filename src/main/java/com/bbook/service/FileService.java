package com.bbook.service;

import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FileService {
	public String uploadFile(String uploadPath,
			String originalFileName, byte[] fileData) throws Exception {
		UUID uuid = UUID.randomUUID();
		String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
		String savedFileName = uuid.toString() + extension;
		String fileUploadFullUrl = uploadPath + "/" + savedFileName;

		FileOutputStream fos = new FileOutputStream(fileUploadFullUrl);
		fos.write(fileData);
		fos.close();

		return savedFileName;
	}

	public void deleteFile(String uploadPath, String fileName) throws Exception {
		String fullPath = uploadPath + "/" + fileName;
		File file = new File(fullPath);
		if (file.exists()) {
			file.delete();
		}
	}
}
