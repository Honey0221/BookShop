$(document).ready(function() {
  // 전체 선택 체크박스
  $('.select-all-reviews').on('change', function() {
    const isChecked = $(this).prop('checked');
    $('.review-checkbox').prop('checked', isChecked);
  });

  // 개별 체크박스
  $('.review-checkbox').on('change', function() {
    // 모든 체크박스가 선택되었는지 확인
    const allChecked = $('.review-checkbox:checked').length === $('.review-checkbox').length;
    $('.select-all-reviews').prop('checked', allChecked);
  });

  // 선택 삭제 버튼
  $('#deleteSelectedReviews').on('click', function() {
    const selectedIds = $('.review-checkbox:checked').map(function() {
      return $(this).data('review-id');
    }).get();

    if (selectedIds.length === 0) return;

    Swal.fire({
      title: '삭제 확인',
      text: `선택한 ${selectedIds.length}개의 리뷰를 삭제하시겠습니까?`,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: '삭제',
      cancelButtonText: '취소'
    }).then((result) => {
      if (result.isConfirmed) {
        deleteSelectedReviews(selectedIds);
      }
    });
  });

  // 선택된 리뷰 삭제 처리
  function deleteSelectedReviews(reviewIds) {
    $.ajax({
      url: '/admin/api/reviews/batch-delete',
      type: 'Post',
      contentType: 'application/json',
      data: JSON.stringify(reviewIds),
      success: function() {
        showAlert('선택한 리뷰가 삭제되었습니다', 'success')
          .then(() => {
            location.reload();
          });
      },
      error: function() {
        showAlert('리뷰 삭제에 실패했습니다', 'warning');
      }
    });
  }

  // 검색 폼 제출 이벤트
  $('#searchForm').on('submit', function(e) {
    e.preventDefault();

    const searchType = $(this).find('[name="searchType"]').val();
    const keyword = $(this).find('[name="keyword"]').val();

    if (!keyword.trim()) {
      return;
    }

    location.href = `/admin/reviewMng?searchType=${searchType}&keyword=${keyword}&page=0`;
  });

  // 신고 승인 버튼 이벤트
  $('.accept-btn').on('click', function() {
    const reportId = $(this).data('report-id');

    Swal.fire({
      title: '신고 승인',
      text: '이 신고를 승인하시겠습니까? 해당 리뷰가 삭제됩니다.',
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: '승인',
      cancelButtonText: '취소'
    }).then((result) => {
      if (result.isConfirmed) {
        processReport(reportId, 'ACCEPTED');
      }
    });
  });

  // 신고 거절 버튼 이벤트
  $('.reject-btn').on('click', function() {
    const reportId = $(this).data('report-id');

    Swal.fire({
      title: '신고 거절',
      text: '이 신고를 거절하시겠습니까?',
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: '거절',
      cancelButtonText: '취소'
    }).then((result) => {
      if (result.isConfirmed) {
        processReport(reportId, 'REJECTED');
      }
    });
  });

  // 신고 처리 함수
  function processReport(reportId, status) {
    $.ajax({
      url: `/admin/api/reports/${reportId}/process`,
      type: 'Post',
      data: { status, status },
      success: function(response) {
        const reviewId = response.reviewId;
        const removedRows = $(`tr[data-review-id="${reviewId}"]`);
        const removedCount = removedRows.length; // 삭제될 행의 개수
        // 같은 리뷰에 대한 모든 신고 행 삭제
        removedRows.fadeOut(300, function() {
          $(this).remove();

          // 테이블에 행이 없으면 "신고 내역이 없습니다" 메시지 표시
          if ($('#reports tbody tr').length === 0) {
            $('#reports tbody').html(
              '<tr><td colspan="7" class="text-center py-5 text-muted">' +
              '신고 내역이 없습니다.</td></tr>'
            );
          }
        });

        // 신고 수 뱃지 업데이트
        updateReportCount(removedCount);
        showAlert('신고가 처리되었습니다', 'success');
      },
      error: function() {
        showAlert('신고 처리에 실패했습니다', 'warning');
      }
    });
  }

  function updateReportCount(removedCount) {
    const currentCount = parseInt($('#reports-tab .badge').text());
    if (!isNaN(currentCount) && currentCount > 0) {
      const newCount = Math.max(0, currentCount - removedCount);
      $('#reports-tab .badge').text(newCount);
    }
  }

  // 탭 변경 시 URL 해시 업데이트
  $('button[data-bs-toggle="tab"]').on('shown.bs.tab', function(e) {
    const hash = $(e.target).attr('data-bs-target');
    if (history.pushState) {
      history.pushState(null, null, hash);
    } else {
      location.hash = hash;
    }
  });

  // 페이지 로드 시 URL 해시에 따른 탭 활성화
  const hash = window.location.hash;
  if (hash) {
    $(`button[data-bs-target="${hash}"]`).tab('show');
  }
});

function showAlert(title, icon, text = '') {
  return Swal.fire({
    title: title,
    text: text,
    icon: icon,
    confirmButtonText: '확인'
  });
}