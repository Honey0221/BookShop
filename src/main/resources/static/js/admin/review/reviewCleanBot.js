$(document).ready(function() {
  // 분석 버튼 이벤트
  $('#analyzeAllBtn').click(function() {
    analyzeReview();
  });

  // 삭제 버튼 이벤트
  $('.delete-btn').click(function() {
    const reviewId = $(this).data('review-id');
    deleteReview(reviewId);
  });
});

function analyzeReview() {
  const $btn = $('#analyzeAllBtn');

  Swal.fire({
    title: '리뷰 분석',
    text: '리뷰 분석을 시작하시겠습니까?',
    icon: 'question',
    showCancelButton: true,
    confirmButtonText: '시작',
    cancelButtonText: '취소'
  }).then((result) => {
    if (result.isConfirmed) {
      // 버튼 비활성화 및 로딩 표시
      $btn.prop('disabled', true);
      $btn.html('<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> 분석중...');

      $.ajax({
        url: '/admin/api/reviews/analyze',
        type: 'Post',
        success: function(response) {
          showAlert('리뷰 분석이 완료되었습니다.', 'success')
            .then(() => {
              location.reload();
            });
        },
        error: function(error) {
          console.error('Error: ', error);
          showAlert('리뷰 분석이 실패하였습니다.', 'error');
        },
        complete: function() {
          // 버튼 상태 복원
          $btn.prop('disabled', false);
          $btn.html('리뷰 분석하기');
        }
      });
    }
  });
}

function deleteReview(reviewId) {
  if (!reviewId) {
    console.error('Review Id is missing');
    return;
  }

  Swal.fire({
    title: '리뷰를 삭제하시겠습니까?',
    icon: 'warning',
    showCancelButton: true,
    confirmButtonText: '삭제',
    cancelButtonText: '취소'
  }).then((result) => {
    if (result.isConfirmed) {
      $.ajax({
        url: `/admin/api/reviews/${reviewId}`,
        type: 'Delete',
        success: function() {
          showAlert('리뷰 삭제가 완료되었습니다.', 'success')
            .then(() => {
              const currentUrl = new URL(window.location.href);
              const currentPage = currentUrl.searchParams.get('page') || 0;

              window.location.href = `/admin/reviewCleanBot?page=${currentPage}`;
            });
        },
        error: function(error) {
          console.log('Error : ', error);
          showAlert('리뷰 삭제가 실패되었습니다.', 'error');
        }
      });
    }
  });
}

function showAlert(title, icon, text = '') {
  return Swal.fire({
      title: title,
      text: text,
      icon: icon,
      confirmButtonText: '확인'
  });
}