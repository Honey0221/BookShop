$(document).ready(function() {
    // Swiper 초기화
    new Swiper('.personalized-swiper', {
        slidesPerView: 'auto',
        spaceBetween: 20,
        navigation: {
            nextEl: '.swiper-button-next',
            prevEl: '.swiper-button-prev',
        },
        pagination: {
            el: '.swiper-pagination',
            clickable: true,
        },
        breakpoints: {
            320: {
                slidesPerView: 2,
                spaceBetween: 10
            },
            480: {
                slidesPerView: 3,
                spaceBetween: 15
            },
            768: {
                slidesPerView: 4,
                spaceBetween: 20
            },
            1024: {
                slidesPerView: 5,
                spaceBetween: 20
            }
        }
    });

    // 스크롤 이벤트 처리
    $(window).scroll(function() {
        var scrollTop = $(window).scrollTop();
        
        // 상단 헤더 고정
        if (scrollTop > 192) {
            $('.cart_top_wrap').addClass('sps-blw');
            $('.cart_content_wrap').css('margin-top', $('.cart_top_wrap').outerHeight());
        } else {
            $('.cart_top_wrap').removeClass('sps-blw');
            $('.cart_content_wrap').css('margin-top', 0);
        }
        
        // TOP 버튼 표시/숨김
        if (scrollTop > 500) {
            $('.btn_go_top').addClass('active');
        } else {
            $('.btn_go_top').removeClass('active');
        }
    });

    // TOP 버튼 클릭 이벤트
    $('.btn_go_top').click(function(e) {
        e.preventDefault();
        $('html, body').animate({scrollTop: 0}, 300);
    });

    // 기존 코드 유지
    calculateCheckedPrice();
    
    $("input[name=cartChkBox]").change(function() {
        calculateCheckedPrice();
    });

    // 배송비 안내 버튼 클릭 이벤트

    $(".btn_shipping_info").click(function() {
        $("#shippingInfoModal").show();
    });

    // 모달 외부 클릭 시 닫기
    $(window).click(function(e) {
        if (e.target == $("#shippingInfoModal")[0]) {
            $("#shippingInfoModal").hide();
        }
        if (e.target == $("#pointsInfoModal")[0]) {
            $("#pointsInfoModal").hide();
        }
    });
});

// 체크된 상품만의 금액 계산 함수
function calculateCheckedPrice() {
    var orderTotalPrice = 0;
    var deliveryFee = 0;

    // 체크된 상품만의 주문 금액 계산 (순수 상품 금액)
    $("input[name=cartChkBox]:checked").each(function () {
        var cartBookId = $(this).val();
        var price = $("#price_" + cartBookId).data("price");
        var count = $("#count_" + cartBookId).val();
        var cartBookTotal = price * count;
        orderTotalPrice += cartBookTotal;
        
        // 개별 상품의 총 금액 업데이트 (배송비 제외)
        $("#totalPrice_" + cartBookId).html(cartBookTotal.toLocaleString() + "원");
    });

    // 구독자 여부 확인
    var isSubscriber = $(".order_summary").data("is-subscriber") === true;

    // 배송비 계산 (구독자는 무료, 일반회원은 15,000원 미만 주문 시 3,000원)
    if (!isSubscriber && orderTotalPrice > 0 && orderTotalPrice < 15000) {
        deliveryFee = 3000;
    }

    // 최종 결제 금액 계산
    var finalAmount = orderTotalPrice + deliveryFee;

    // 화면 업데이트
    $("#totalPrice").html(orderTotalPrice.toLocaleString());  // 순수 상품 금액
    $("#deliveryFee").html(deliveryFee.toLocaleString());    // 배송비
    $("#orderTotalPrice").html(finalAmount.toLocaleString());  // 최종 결제 금액
    
    // 적립 예정 포인트 계산 및 표시 (구독자 10%, 일반회원 5% 적립)
    var pointRate = isSubscriber ? 0.1 : 0.05;
    var expectedPoints = Math.floor(finalAmount * pointRate);
    $("#expectedPoints").html(expectedPoints.toLocaleString());
}

// 전체 선택/해제 토글 함수
function checkAll() {
    if ($("#checkall").prop("checked")) {
        $("input[name=cartChkBox]").prop("checked", true);
    } else {
        $("input[name=cartChkBox]").prop("checked", false);
    }
    calculateCheckedPrice();
}

// 개별 체크박스 해제 시 전체 선택 체크박스 상태 업데이트
function uncheck() {
    if (!$("input[name=cartChkBox]").prop("checked")) {
        $("#checkall").prop("checked", false);
    }
    calculateCheckedPrice();
}

// 수량 증가
function increaseCount(button) {
    const cartBookId = button.getAttribute('data-id');
    const input = document.querySelector(`#count_${cartBookId}`);
    const stock = parseInt(button.getAttribute('data-stock'));
    const currentValue = parseInt(input.value);
    
    if (currentValue >= stock) {
        Swal.fire({
            icon: 'warning',
            title: '재고 부족',
            text: '재고가 부족합니다.',
            confirmButtonColor: '#4E73DF'
        });
        return;
    }
    
    input.value = currentValue + 1;
    updateQuantity(input);
}

// 수량 감소
function decreaseCount(button) {
    const cartBookId = button.getAttribute('data-id');
    const input = document.querySelector(`#count_${cartBookId}`);
    const currentValue = parseInt(input.value);
    
    if (currentValue > 1) {
        input.value = currentValue - 1;
        updateQuantity(input);
    }
}

// 수량 변경 시 호출되는 함수
function changeCount(input) {
    const cartBookId = input.getAttribute('data-id');
    const stock = parseInt(input.getAttribute('data-stock'));
    let newValue = parseInt(input.value);
    
    if (isNaN(newValue) || newValue < 1) {
        newValue = 1;
    } else if (newValue > stock) {
        Swal.fire({
            icon: 'warning',
            title: '재고 부족',
            text: '재고가 부족합니다.',
            confirmButtonColor: '#4E73DF'
        });
        newValue = stock;
    }
    
    input.value = newValue;
    updateQuantity(input);
}

// 선택된 상품 삭제
function deleteSelectedCartBooks() {
    var selectedCartBooks = [];
    $("input[name=cartChkBox]:checked").each(function() {
        selectedCartBooks.push($(this).val());
    });

    if (selectedCartBooks.length === 0) {
        Swal.fire({
            icon: 'warning',
            title: '선택된 상품 없음',
            text: '삭제할 상품을 선택해주세요.',
            confirmButtonColor: '#4E73DF'
        });
        return;
    }

    Swal.fire({
        title: '상품 삭제',
        text: '장바구니에서 삭제하시겠습니까?',
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#4E73DF',
        cancelButtonColor: '#d33',
        confirmButtonText: '삭제',
        cancelButtonText: '취소'
    }).then((result) => {
        if (result.isConfirmed) {

            $.ajax({
                url: '/cart/books',
                type: 'DELETE',
                data: JSON.stringify(selectedCartBooks),
                contentType: 'application/json',
                success: function() {
                    location.reload();
                },
                error: function(jqXHR, textStatus, errorThrown) {
                    Swal.fire({
                        icon: 'error',
                        title: '오류 발생',
                        text: '상품 삭제 중 오류가 발생했습니다.',
                        confirmButtonColor: '#4E73DF'
                    });
                }
            });
        }
    });
}

// 개별 상품 삭제
function deleteCartBook(btn) {
    var cartBookId = $(btn).data("id");
    
    Swal.fire({
        title: '상품 삭제',
        text: '선택하신 상품을 장바구니에서 삭제하시겠습니까?',
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#4E73DF',
        cancelButtonColor: '#d33',
        confirmButtonText: '삭제',
        cancelButtonText: '취소'
    }).then((result) => {
        if (result.isConfirmed) {

            $.ajax({
                url: '/cart/' + cartBookId,
                type: 'DELETE',
                data: JSON.stringify(cartBookId),
                success: function() {
                    location.reload();
                },
                error: function(jqXHR, textStatus, errorThrown) {
                    Swal.fire({
                        icon: 'error',
                        title: '오류 발생',
                        text: '상품 삭제 중 오류가 발생했습니다.',
                        confirmButtonColor: '#4E73DF'
                    });
                }
            });
        }
    });
}

// 선택된 상품 주문 처리 함수
function orders() {
    var checkedCartBooks = $("input[name=cartChkBox]:checked");

    if(checkedCartBooks.length === 0){
        Swal.fire({
            icon: 'warning',
            title: '선택된 상품 없음',
            text: '주문할 상품을 선택해주세요.',
            confirmButtonColor: '#4E73DF'
        });
        return;
    }

    var cartBooks = [];
    checkedCartBooks.each(function() {
        cartBooks.push({
            cartBookId: $(this).val()
        });
    });

    $.ajax({
        url: "/order/payment",
        type: "POST",
        contentType: "application/json",
        data: JSON.stringify({
            cartBooks: cartBooks
        }),
        success: function(response) {
            location.href = '/order/payment';
        },
        error: function(jqXHR) {
            Swal.fire({
                icon: 'error',
                title: '오류 발생',
                text: jqXHR.responseText,
                confirmButtonColor: '#4E73DF'
            });
        }
    });
}

// 모달 닫기 함수
function closeModal() {
    $("#shippingInfoModal").hide();
}

// 장바구니 목록 토글 함수
function toggleCartList(button) {
    const container = document.querySelector('.cart_items_container');
    const icon = button.querySelector('i');
    
    if (container.classList.contains('expanded')) {
        // 접기
        container.classList.remove('expanded');
        container.classList.add('collapsed');
        button.classList.add('collapsed');
    } else {
        // 펼치기
        container.classList.remove('collapsed');
        container.classList.add('expanded');
        button.classList.remove('collapsed');
    }
}

// 페이지 로드 시 기본적으로 펼쳐진 상태로 시작
document.addEventListener('DOMContentLoaded', function() {
    const container = document.querySelector('.cart_items_container');
    container.classList.add('expanded');
});

function updateQuantity(input) {
    const cartBookId = input.getAttribute('data-id');
    const stock = parseInt(input.getAttribute('data-stock'));
    const quantity = parseInt(input.value) || 1;
    
    if (quantity > stock) {
        Swal.fire({
            icon: 'warning',
            title: '재고 부족',
            text: '재고가 부족합니다.',
            confirmButtonColor: '#4E73DF'
        });
        input.value = stock;
        return;
    }
    
    if (quantity < 1) {
        input.value = 1;
        return;
    }
    
    $.ajax({
        url: `/cartBook/${cartBookId}`,
        type: 'PATCH',
        contentType: 'application/json',
        data: JSON.stringify({ 
            count: quantity 
        }),
        success: function(result) {
            // 개별 상품 금액 업데이트
            const price = $("#price_" + cartBookId).data("price");
            const totalPrice = price * quantity;
            $("#totalPrice_" + cartBookId).html(totalPrice.toLocaleString() + "원");
            
            // 전체 금액 재계산
            calculateCheckedPrice();
        },
        error: function(error) {
            console.error('수량 업데이트 실패:', error);
            Swal.fire({
                icon: 'error',
                title: '오류 발생',
                text: '수량 변경에 실패했습니다.',
                confirmButtonColor: '#4E73DF'
            });
        }
    });
}

function updateCartBookPrice(cartBookId) {
    const countInput = document.querySelector(`#count_${cartBookId}`);
    const priceElement = document.querySelector(`#price_${cartBookId}`);
    const totalPriceElement = document.querySelector(`#totalPrice_${cartBookId}`);
    
    const quantity = parseInt(countInput.value);
    const price = parseInt(priceElement.getAttribute('data-price'));
    const totalPrice = quantity * price;  // 순수 상품 금액만 계산
    
    totalPriceElement.textContent = totalPrice.toLocaleString() + '원';
}

function updateTotalPrice() {
    var orderTotalPrice = 0;
    var deliveryFee = 0;

    // 체크된 상품만의 주문 금액 계산
    $("input[name=cartChkBox]:checked").each(function () {
        var cartBookId = $(this).val();
        var price = $("#price_" + cartBookId).data("price");
        var count = $("#count_" + cartBookId).val();
        orderTotalPrice += price * count;
    });

    // 배송비 계산 (15,000원 미만 주문 시 3,000원)
    if(orderTotalPrice > 0 && orderTotalPrice < 15000) {
        deliveryFee = 3000;
    }

    // 화면 업데이트
    $("#totalPrice").html(orderTotalPrice.toLocaleString());
    $("#deliveryFee").html(deliveryFee.toLocaleString());
    $("#orderTotalPrice").html((orderTotalPrice + deliveryFee).toLocaleString());
}

// 배송비 안내 모달 열기
function openShippingModal() {
    $("#shippingInfoModal").show();
}

// 포인트 안내 모달 열기
function openPointsModal() {
    $("#pointsInfoModal").show();
}

// 포인트 안내 모달 닫기
function closePointsModal() {
    $("#pointsInfoModal").hide();
}
