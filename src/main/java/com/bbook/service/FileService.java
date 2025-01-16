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

		// 업로드 경로에 따라 다른 URL 반환
		if (uploadPath.contains("itemImgLocation")) {
			return "/bookshop/book/" + savedFileName;
		} else if (uploadPath.contains("reviewImgLocation")) {
			return "/bookshop/review/" + savedFileName;
		}

		return savedFileName;
	}

	public void deleteFile(String uploadPath, String fileName) {
		// 파일명에 URL 경로가 포함된 경우
		String actualFileName = fileName;
		if (fileName.startsWith("/boopshop/")) {
			actualFileName = fileName.substring(fileName.lastIndexOf("/") + 1);
		}

		String fullPath = uploadPath + "/" + actualFileName;
		File file = new File(fullPath);
		if (file.exists()) {
			file.delete();
		}
	}
}
