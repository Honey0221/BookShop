function updateAvgRating(bookId) {
  $.ajax({
    url: `/reviews/average/${bookId}`,
    type: "Get",
    success: function (avgRating) {
      $(".rating-value").text(avgRating.toFixed(1));
    }
  });
}

function updateReviewCount() {
  let bookId = $("#bookId").val();
  $.ajax({
    url: `/reviews/count/${bookId}`,
    type: "Get",
    success: function (count) {
      $("#review-tab").text(`리뷰(${count})`);
    }
  });
}

function showAlert(title, icon = '') {
  return Swal.fire({
    title: title,
    icon: icon,
    confirmButtonText: '확인'
  });
}

$(document).ready(function() {
  updateReviewCount();

  $("#review-tab").on("click", function (e) {
    loadReviews();
  });

  $("#reviewForm").on("submit", function (e) {
    e.preventDefault();

    let reviewFormData = {
      bookId: $("#bookId").val(),
      rating: $("#rating").val(),
      content: $("#content").val()
    }

    console.log("리뷰 데이터 : ", reviewFormData);
    $.ajax({
      url: "/reviews",
      type: "Post",
      contentType: "application/json",
      data: JSON.stringify(reviewFormData),
      success: function (result) {
        const modal = bootstrap.Modal.getInstance(document.getElementById('reviewModal'));
        modal.hide();

        $("#content").val("");
        $("#rating").val('5');
        loadReviews();

        updateAvgRating(reviewFormData.bookId);
        updateReviewCount();

        Swal.fire({
          title: "리뷰가 등록되었습니다.",
          icon: "success",
          confirmButtonText: "확인"
        });
      },
      error: function () {
        Swal.fire({
          title: "리뷰 등록에 실패했습니다.",
          icon: "error",
          confirmButtonText: "확인"
        });
      }
    });
  });

  $("#reviewModal").on("hidden.bs.modal", function () {
    $("#reviewForm")[0].reset();
  });

  const price = parseInt($("#totalPrice").text().replace(/[^0-9]/g, ""));

  // 수량 변경 시 총 가격 업데이트
  $("#quantity").change(function () {
    const quantity = parseInt($(this).val());
    if (quantity < 1) {
      $(this).val(1);
      updateTotalPrice(1);
    } else {
      updateTotalPrice(quantity);
    }
  });

  // 수량 입력 시 총 가격 업데이트
  $("#quantity").on('input', function () {
    const quantity = parseInt($(this).val()) || 0;
    if (quantity < 1) {
      $(this).val(1);
      updateTotalPrice(1);
    } else {
      updateTotalPrice(quantity);
    }
  });

  // 총 가격 업데이트 함수
  function updateTotalPrice(quantity) {
    const totalPrice = price * quantity;
    $("#totalPrice").text(totalPrice.toLocaleString() + '원');
  }

  if (!Kakao.isInitialized()) {
    Kakao.init('d3524ffef60bb1c59553b6a24dd4ef1d');
  }

  $("#shareKakaoBtn").click(function () {
    try {
      Kakao.Share.sendDefault({
        objectType: 'commerce',
        content: {
          title: $(".book-title").text(),
          imageUrl: $(".book-img img").attr("src"),
          link: {
            mobileWebUrl: window.location.href,
            webUrl: window.location.href,
          },
        },
        commerce: {
          productName: $(".book-author").text(),
          regularPrice: parseInt($(".book-price span:last").text().replace(/[^0-9]/g, "")),
        },
        buttons: [
          {
            title: '구매하기',
            link: {
              mobileWebUrl: window.location.href,
              webUrl: window.location.href,
            },
          },
        ],
      });
    } catch (error) {
      console.error('카카오톡 공유 실패 : ', error);
      Swal.fire({
        title: '카카오톡 공유 실패',
        confirmButtonText: '확인'
      });
    }
  });

  $('#loginBtn').click(function() {
    showAlert('로그인이 필요합니다', 'warning')
      .then((result) => {
        if (result.isConfirmed) {
          window.location.href = '/members/login';
        }
      });
  });

  // 찜하기 버튼 클릭 이벤트
  $('#wishBtn').click(function() {
    const bookId = $('#bookId').val();

    $.ajax({
      url: `/wish/${bookId}`,
      type: 'Post',
      success: function(response) {
        const $icon = $('#wishBtn i');
        if (response.isWished) {
          // 찜하지 않은 상태라면
          $icon.removeClass('far').addClass('fas');
          showAlert('찜 목록에 추가되었습니다.', 'success');
        } else {
          // 이미 찜한 상태라면
          $icon.removeClass('fas').addClass('far');
          showAlert('찜 목록에서 제거되었습니다.', 'success');
        }
      },
      error: function() {
        showAlert('오류가 발생했습니다.', 'error');
      }
    });
  });
});

// 현재 페이지 번호
let currentPage = 0;

// 리뷰 목록 조회
function loadReviews(page = 0) {
  // 시간 표시 형식 변환
  function formatTimeAgo(dateString) {
    const date = new Date(dateString);
    const now = new Date();
    const diffInSeconds = Math.floor((now - date) / 1000);
    const diffInMinutes = Math.floor(diffInSeconds / 60);
    const diffInHours = Math.floor(diffInMinutes / 60);
    const diffInDays = Math.floor(diffInHours / 24);

    if (diffInSeconds < 60) {
      return "방금 전";
    } else if (diffInMinutes < 60) {
      return `${diffInMinutes}분 전`;
    } else if (diffInHours < 24) {
      return `${diffInHours}시간 전`;
    } else if (diffInHours < 7) {
      return `${diffInDays}일 전`;
    } else {
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      return `${year}-${month}-${day}`;
    }
  }

  const bookId = $("#bookId").val();
  $.ajax({
    url: `/reviews/${bookId}?page=${page}`,
    type: "Get",
    dataType: "json",
    success: function (response) {
      $("#reviewContainer").empty();
      updateReviewCount();
      response.content.forEach(function (review) {
        const stars = "★".repeat(review.rating) + "☆".repeat(5 - review.rating);
        const timeAgo = formatTimeAgo(review.createdAt);
        // 수정/삭제 버튼(자기만 보이게)
        let editDeleteBtn = '';
        if (review.isOwner === true) {
          editDeleteBtn = `
          <div class="brn-group">
            <button class="btn btn-sm btn-primary edit-review me-1"
                          data-review-id="${review.id}"
                          data-rating="${review.rating}"
                          data-content="${review.content}">
              <i class="fas fa-edit"></i>
            </button>
            <button class="btn btn-sm btn-danger delete-review"
                          data-review-id="${review.id}">
            <i class="fas fa-trash"></i>
            </button>
          </div>`;
        }

        const reviewHtml = `
          <div class="review-item border-bottom py-3">
            <div class="d-flex justify-content-between align-items-start">
              <div>
                <div class="rating text-warning">${stars}</div>
                <div class="reviewer mb-2">${review.memberName}</div>
              </div>
              <div class="d-flex align-items-center gap-2">
                <small class="text-muted">${timeAgo}</small>
                ${editDeleteBtn}
              </div>
            </div>
            <div class="review-content">${review.content}</div>
          </div>
        `;
        $("#reviewContainer").append(reviewHtml);
      });

      let paginationHtml = '<div class="d-flex justify-content-center mt-4"><ul class="pagination">';

      if (!response.first) {
        paginationHtml += `<li class="page-item">
                              <a class="page-link" href="#" data-page="${response.number - 1}">이전</a>
                           </li>`;
      }

      for (let i = 0; i < response.totalPages; i++) {
        paginationHtml += `<li class="page-item ${response.number === i ? 'active' : ''}">
                              <a class="page-link" href="#" data-page="${i}">${i + 1}</a>
                           </li>`;
      }

      if (!response.last) {
        paginationHtml += `<li class="page-item">
                              <a class="page-link" href="#" data-page="${response.number + 1}">다음</a>
                           </li>`;
      }

      paginationHtml += '</ul></div>';
      $("#reviewContainer").append(paginationHtml);

      $(".pagination .page-link").on("click", function (e) {
        e.preventDefault();
        const page = $(this).data("page");
        currentPage = page;
        loadReviews(page);
      })

      // 수정 버튼 이벤트
      $(".edit-review").on("click", function () {
        const reviewId = $(this).data("review-id");
        editReview(reviewId);
      })

      // 삭제 버튼 이벤트
      $(".delete-review").on("click", function () {
        const reviewId = $(this).data("review-id");
        deleteReview(reviewId);
      });
    },
    error: function () {
      Swal.fire({
        title: "리뷰 목록을 불러오는데 실패했습니다.",
        confirmButtonText: "확인"
      });
    }
  });
}

// 리뷰 수정 함수
function editReview(reviewId) {
  const rating = $(`.edit-review[data-review-id="${reviewId}"]`).data('rating');
  const content = $(`.edit-review[data-review-id="${reviewId}"]`).data('content');

  console.log("수정 시작 - 리뷰 아이디 : ", reviewId);
  console.log("기존 데이터 - rating : ", rating, "content : ", content);

  $('#editReviewId').val(reviewId);
  $('#editRating').val(rating);
  $('#editContent').val(content);

  $('#editReviewModal').modal('show');

  $('#editReviewForm').off('submit').on('submit', function (e) {
    e.preventDefault();

    const editFormData = {
      rating: $('#editRating').val(),
      content: $('#editContent').val()
    };

    console.log("전송할 데이터 : ", editFormData);

    $.ajax({
      url: `/reviews/${reviewId}`,
      type: 'Patch',
      contentType: 'application/json',
      data: JSON.stringify(editFormData),
      success: function (result) {
        console.log("수정 성공 ", result);
        $('#editReviewModal').modal('hide');
        loadReviews();
        updateAvgRating($("#bookId").val());
        updateReviewCount();

        Swal.fire({
          title: '리뷰가 수정되었습니다.',
          icon: 'success',
          confirmButtonText: '확인'
        });
      },
      error: function (error) {
        console.log("수정 실패 ", error);
        Swal.fire({
          title: '리뷰 수정에 실패했습니다.',
          icon: 'error',
          confirmButtonText: '확인'
        });
      }
    });
  });
}

// 수정 모달창이 닫힐 때 폼 초기화
$('#editReviewModal').on('hidden.bs.modal', function () {
  $('#editReviewForm')[0].reset();
});

// 리뷰 삭제 함수
function deleteReview(reviewId) {
  Swal.fire({
    title: '리뷰를 삭제하시겠습니까?',
    text: '삭제된 리뷰는 복구할 수 없습니다.',
    icon: 'warning',
    showCancelButton: true,
    confirmButtonColor: '#d33',
    cancelButtonColor: '#3085d6',
    confirmButtonText: '삭제',
    cancelButtonText: '취소'
  }).then((result) => {
    if (result.isConfirmed) {
      let reviewFormData = {
        bookId: $("#bookId").val(),
        rating: $("#rating").val(),
        content: $("#content").val()
      }
      $.ajax({
        url: `/reviews/${reviewId}`,
        type: "Delete",
        contentType: "application/json",
        data: JSON.stringify(reviewFormData),
        success: function () {
          loadReviews();
          updateAvgRating($("#bookId").val());
          updateReviewCount();

          Swal.fire({
            title: '삭제되었습니다.',
            icon: 'success',
            confirmButtonText: '확인'
          });
        },
        error: function () {
          Swal.fire({
            title: '리뷰 삭제에 실패했습니다.',
            icon: 'error',
            confirmButtonText: '확인'
          });
        }
      });
    }
  });
}


function order() {
  const bookId = document.getElementById('bookId').value;
  const quantity = document.getElementById('quantity').value;
  const price = parseInt($("#totalPrice").text().replace(/[^0-9]/g, ""));

  const paramData = {
    bookId: Number(bookId),
    count: parseInt(quantity),
    totalPrice: price
  }

  $.ajax({
    url: "/order/payment",
    type: "POST",
    contentType: "application/json",
    data: JSON.stringify(paramData),
    success: function(response) {
      location.href = '/order/payment';
    },
    error: function(jqXHR) {
      if(jqXHR.status == '401') {
        if(confirm('로그인이 필요한 서비스입니다.\n로그인 페이지로 이동하시겠습니까?')) {
          location.href = '/members/login';
        }
      } else {
        alert(jqXHR.responseText);
      }
    }
  });
}

function addCart() {
  // bookId 값을 hidden input에서 가져오기
  const bookId = document.getElementById('bookId').value;
  console.log("Raw bookId value:", bookId);

  const quantity = document.getElementById('quantity').value;
  console.log("Raw quantity value:", quantity);


  const url = "/cart";
  const paramData = {
    bookId: Number(bookId),
    count: parseInt(quantity)
  };

  console.log("전송할 데이터:", paramData);

  $.ajax({
    url: url,
    type: "POST",
    contentType: "application/json",
    data: JSON.stringify(paramData),

    success: function(result, status) {
      alert("상품을 장바구니에 담았습니다.");
    },
    error: function(jqXHR, status, error) {
      console.log("에러 발생:", error);
      if(jqXHR.status == '401') {
        if(confirm('로그인이 필요한 서비스입니다.\n로그인 페이지로 이동하시겠습니까?')) {
          location.href = '/members/login';
        }
      } else {
        alert(jqXHR.responseText);
      }
    }
  });
}