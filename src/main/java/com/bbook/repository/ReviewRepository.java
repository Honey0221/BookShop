package com.bbook.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.bbook.dto.ReviewDto;
import com.bbook.entity.Reviews;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ReviewRepository {
	private final JdbcTemplate jdbcTemplate;

	public void save(Reviews reviews) {
		String sql = "INSERT INTO reviews (member_id, book_id, rating, content, "
				+ "created_at) VALUES (?, ?, ?, ?, ?)";

		jdbcTemplate.update(sql,
				reviews.getMember_id(),
				reviews.getBook_id(),
				reviews.getRating(),
				reviews.getContent(),
				LocalDateTime.now()
		);
	}

	public List<ReviewDto> findByBookId(Long bookId) {
		String sql = "SELECT r.*, m.name as member_name FROM reviews r " +
				"LEFT JOIN member m ON r.member_id = m.member_id " +
				"WHERE r.book_id = ? " +
				"ORDER BY r.created_at DESC";

		return jdbcTemplate.query(sql,
				(rs, rowNum) -> ReviewDto.builder()
						.id(rs.getLong("id"))
						.memberId(rs.getLong("member_id"))
						.bookId(rs.getLong("book_id"))
						.rating(rs.getInt("rating"))
						.content(rs.getString("content"))
						.memberName(rs.getString("member_name"))
						.createdAt(rs.getTimestamp("created_at").toLocalDateTime())
						.build(),
				bookId
		);
	}
}
