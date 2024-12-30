var token = $("meta[name='_csrf']").attr("content");
var header = $("meta[name='_csrf_header']").attr("content");

$(document).ready(function() {
  $("#review-tab").on("click", function(e) {
    loadReviews();
  });

  $("#reviewForm").on("submit", function(e) {
    e.preventDefault();

    let reviewFormData = {
      bookId: $("#bookId").val(),
      rating: $("#rating").val(),
      content: $("#content").val()
    }

    console.log("데이터 : ", reviewFormData);

    $.ajax({
      url: "/reviews",
      type: "Post",
      contentType: "application/json",
      beforeSend: function(xhr) {
        xhr.setRequestHeader(header, token);
      },
      data: JSON.stringify(reviewFormData),
      success: function(result) {
        Swal.fire({
          title: "리뷰가 등록되었습니다.",
          confirmButtonText: "확인"
        }).then(() => {
          $("#content").val("");
          loadReviews();
        });
      },
      error: function() {
        Swal.fire({
          title: "리뷰 등록에 실패했습니다.",
          confirmButtonText: "확인"
        })
      }
    });
  });

  const price = parseInt($("#totalPrice").text().replace(/[^0-9]/g, ""));

  // 수량 변경 시 총 가격 업데이트
  $("#quantity").change(function() {
    const quantity = parseInt($(this).val());
    if (quantity < 1) {
      $(this).val(1);
      updateTotalPrice(1);
    } else {
      updateTotalPrice(quantity);
    }
  });

  // 수량 입력 시 총 가격 업데이트
  $("#quantity").on('input', function() {
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
    console.log("카카오 SDK 초기화 시작");
    Kakao.init('d3524ffef60bb1c59553b6a24dd4ef1d');
    console.log("초기화 상태 : " , Kakao.isInitialized());
  }

  $("#shareKakaoBtn").click(function() {
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
        title : '카카오톡 공유 실패',
        confirmButtonText : '확인'
      });
    }
  });
});

// 리뷰 목록 조회
function loadReviews() {
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
      return new Date(dateString).toLocaleString('ko-KR', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
      });
    }
  }

  const bookId = $("#bookId").val();

  $.ajax({
    url: "/reviews/" + bookId,
    type: "Get",
    dataType: "json",
    beforeSend: function(xhr) {
      xhr.setRequestHeader(header, token);
    },
    success: function(reviews) {
      $("#reviewContainer").empty();
      reviews.forEach(function(review) {
        const stars = "★".repeat(review.rating) + "☆".repeat(5 - review.rating);
        const timeAgo = formatTimeAgo(review.createdAt);
        const reviewHtml = `
          <div class="review-item border-bottom py-3">
            <div class="d-flex justify-content-between">
              <div class="rating text-warning">${stars}</div>
              <small class="text-muted">${timeAgo}</small>
            </div>
            <div class="reviewer mb-2">${review.memberName}</div>
            <div class="review-content">${review.content}</div>
          </div>
        `;
        $("#reviewContainer").append(reviewHtml);
      });
    },
    error: function() {
      Swal.fire({
        title: "리뷰 목록을 불러오는데 실패했습니다.",
        confirmButtonText: "확인"
      });
    }
  });
}