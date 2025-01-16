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
		File uploadDir = new File(uploadPath);
		if (!uploadDir.exists()) {
			uploadDir.mkdirs();
		}

		String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
		String savedFileName = UUID.randomUUID().toString() + extension;
		String fileUploadFullUrl = uploadPath + "/" + savedFileName;

		System.out.println("파일 저장 경로 : " + fileUploadFullUrl);

		try (FileOutputStream fos = new FileOutputStream(fileUploadFullUrl)) {
			fos.write(fileData);
		}

		return "/bookshop/book/" + savedFileName;
	}

	public void deleteFile(String uploadPath, String fileName) {
		String fullPath = uploadPath + "/" + fileName;
		File file = new File(fullPath);
		if (file.exists()) {
			file.delete();
		}
	}
}
