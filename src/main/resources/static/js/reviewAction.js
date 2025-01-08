// 좋아요 토글 함수
function toggleLike(reviewId) {
  $.ajax({
    url: `/reviews/${reviewId}/like`,
    type: 'POST',
    success: function(result) {
      // 좋아요 UI 업데이트
      const likeButton = $(`#like-${reviewId}`);
      const likeCount = $(`#like-count-${reviewId}`);

      if (result.isLiked) {
          likeButton.addClass('liked');
      } else {
          likeButton.removeClass('liked');
      }
      likeCount.text(result.likeCount);
    },
    error: function(xhr, error) {
      if (xhr.status === 401) {
        showAlert('로그인이 필요합니다.', 'info')
          .then((result) => {
            if (result.isConfirmed) {
              window.location.href = '/members/login';
            }
          });
      } else {
        showAlert('좋아요 처리 중 오류가 발생했습니다.', 'error');
      }
    }
  });
}

// 리뷰 신고 모발창 함수
function reportReview(reviewId) {
  $.get('/reviews/report').done(function() {
    Swal.fire({
      title: '리뷰 신고',
      html: `
        <select id="reportType" class="form-select mb-3">
          <option value="">신고 유형</option>
          <option value="SPAM">스팸/광고</option>
          <option value="INAPPROPRIATE">부적절한 내용</option>
          <option value="HATE_SPEECH">혐오 발언</option>
          <option value="FALSE_INFO">허위 정보</option>
          <option value="OTHER">기타</option>
        </select>
        <textarea id="reportContent" class="form-control"
                  placeholder="신고 내용 입력하세요" rows="3"></textarea>
      `,
      showCancelButton: true,
      confirmButtonText: '신고',
      cancelButtonText: '취소',
      preConfirm: () => {
        const reportType = $('#reportType').val();
        const content = $('#reportContent').val();

        if (!reportType) {
          Swal.showValidationMessage('신고 유형을 선택해주세요');
          return false;
        }
        if (!content) {
          Swal.showValidationMessage('신고 내용을 입력해주세요');
          return false;
        }

        return { reportType, content };
      }
    }).then((result) => {
      if (result.isConfirmed) {
        $.ajax({
          url: '/reviews/report',
          type: 'Post',
          data: {
            reviewId: reviewId,
            reportType: result.value.reportType,
            content: result.value.content
          },
          success: function() {
            showAlert('신고가 접수되었습니다.', 'success');
          },
          error: function(xhr) {
            if (xhr.status === 400) {
              showAlert('이미 신고한 리뷰입니다.', 'warning');
            } else {
              showAlert('신고 처리 중 오류가 발생했습니다.', 'error');
            }
          }
        });
      }
    });
  }).fail(function(xhr) {
    if (xhr.status === 401) {
      showAlert('로그인이 필요합니다.', 'info')
        .then((result) => {
          if (result.isConfirmed) {
            window.location.href = '/members/login';
          }
        });
    }
  });
}