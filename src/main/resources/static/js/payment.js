// IMP 초기화
(function() {
    var IMP = window.IMP;
    IMP.init("imp80047713");
})();

$(document).ready(function() {

    // 스크롤 이벤트 처리
    $(window).scroll(function() {
        var scrollTop = $(window).scrollTop();
        
        // 상단 헤더 고정
        if (scrollTop > 192) {
            $('.payment_top_wrap').addClass('sps-blw');
            $('.payment_content_wrap').css('margin-top', $('.payment_top_wrap').outerHeight());
        } else {
            $('.payment_top_wrap').removeClass('sps-blw');
            $('.payment_content_wrap').css('margin-top', '40px');
        }
    });

    // 초기 금액 계산
    calculateTotalPrice();

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

// 결제 금액 계산 함수
function calculateTotalPrice() {
    var originalPrice = parseInt($('#productPrice').text().replace(/[^0-9]/g, ''));
    var pointDiscount = parseInt($('#pointDiscount').text().replace(/[^0-9]/g, '')) || 0;
    var couponDiscount = parseInt($('#couponDiscount').text().replace(/[^0-9]/g, '')) || 0;
    var shippingFee = 0;
    var finalPrice = 0;
    
    // 구독자 여부 확인 후 배송비 계산
    $.ajax({
        url: '/subscription/check',
        method: 'GET',
        async: false, // 동기 처리로 변경
        success: function(response) {
            // 구독자는 무료배송, 비구독자는 15000원 미만일 때 3000원
            shippingFee = response.isSubscriber ? 0 : (originalPrice < 15000 ? 3000 : 0);
            
            // 배송비 표시
            $('#deliveryFee').text(shippingFee.toLocaleString() + '원');

            // 최종 금액 계산
            finalPrice = originalPrice - pointDiscount - couponDiscount + shippingFee;
            $('#finalTotalPrice').text(finalPrice.toLocaleString() + '원');

            // 적립 예정 포인트 계산 (구독자 10%, 일반회원 5%)
            var pointRate = response.isSubscriber ? 0.1 : 0.05;
            var expectedPoints = Math.floor(finalPrice * pointRate);
            $('#expectedPoints').text(expectedPoints.toLocaleString() + 'P');
        },
        error: function() {
            // 에러 시 기본 배송비 정책 적용
            shippingFee = originalPrice < 15000 ? 3000 : 0;
            $('#deliveryFee').text(shippingFee.toLocaleString() + '원');

            // 최종 금액 계산
            finalPrice = originalPrice - pointDiscount - couponDiscount + shippingFee;
            $('#finalTotalPrice').text(finalPrice.toLocaleString() + '원');

            // 에러 시 기본값으로 5% 적립
            var expectedPoints = Math.floor(finalPrice * 0.05);
            $('#expectedPoints').text(expectedPoints.toLocaleString() + 'P');
        }
    });

    return {
        originalPrice: originalPrice,
        pointDiscount: pointDiscount,
        couponDiscount: couponDiscount,
        shippingFee: shippingFee,
        finalPrice: finalPrice
    };
}

// 포인트 적용
function applyPoint() {
    var pointInput = $('#usePoint');
    var usePoint = parseInt(pointInput.val()) || 0;
    var maxPoint = parseInt(pointInput.attr('max'));
    var currentPrice = calculateTotalPrice().finalPrice;

    if (usePoint % 100 !== 0) {
        Swal.fire({
            icon: 'warning',
            title: '포인트 사용',
            text: '포인트는 100P 단위로 사용 가능합니다.',
            confirmButtonColor: '#4E73DF'
        });
        pointInput.val(Math.floor(usePoint / 100) * 100);
        return;
    }

    if (usePoint > maxPoint) {
        Swal.fire({
            icon: 'warning',
            title: '포인트 사용',
            text: '보유 포인트를 초과하여 사용할 수 없습니다.',
            confirmButtonColor: '#4E73DF'
        });
        pointInput.val(maxPoint);
        return;
    }

    if (usePoint > currentPrice) {
        Swal.fire({
            icon: 'warning',
            title: '포인트 사용',
            text: '결제 금액을 초과하여 사용할 수 없습니다.',
            confirmButtonColor: '#4E73DF'
        });
        pointInput.val(currentPrice);
        return;
    }

    $.ajax({
        url: '/order/apply-points',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({ points: usePoint }),
        success: function(response) {
            $('#pointDiscount').text('-' + usePoint.toLocaleString() + '원');
            calculateTotalPrice();
            
            Swal.fire({
                icon: 'success',
                title: '포인트 적용 완료',
                text: usePoint.toLocaleString() + 'P가 적용되었습니다.',
                confirmButtonColor: '#4E73DF'
            });
        },
        error: function(xhr) {
            console.error('포인트 적용 실패:', xhr);
            Swal.fire({
                icon: 'error',
                title: '포인트 적용 실패',
                text: '포인트 적용 중 오류가 발생했습니다.',
                confirmButtonColor: '#4E73DF'
            });
        }
    });
}

// 포인트 취소
function cancelPoint() {
    $('#usePoint').val('');
    $('#pointDiscount').text('0원');
    calculateTotalPrice();
    
    // 세션의 포인트 사용 정보 제거
    $.ajax({
        url: '/order/cancel-points',
        type: 'POST',
        success: function(response) {
            Swal.fire({
                icon: 'success',
                title: '포인트 취소 완료',
                text: '포인트가 취소되었습니다.',
                confirmButtonColor: '#4E73DF'
            });
        },
        error: function(xhr) {
            console.error('포인트 취소 실패:', xhr);
            Swal.fire({
                icon: 'error',
                title: '포인트 취소 실패',
                text: '포인트 취소 중 오류가 발생했습니다.',
                confirmButtonColor: '#4E73DF'
            });
        }
    });
}

// 쿠폰 적용
function applyCoupon() {
    var couponSelect = $('#couponSelect');
    var couponAmount = parseInt(couponSelect.val()) || 0;
    var orderAmount = calculateTotalPrice().originalPrice;

    if (orderAmount < 15000) {
        Swal.fire({
            icon: 'warning',
            title: '쿠폰 사용',
            text: '15,000원 이상 구매 시 쿠폰을 사용할 수 있습니다.',
            confirmButtonColor: '#4E73DF'
        });
        couponSelect.val('');
        return;
    }

    // 세션에 쿠폰 사용 정보 저장
    $.ajax({
        url: '/order/apply-coupon',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({ 
            orderAmount: orderAmount,
            couponAmount: couponAmount
        }),
        success: function(response) {
            if (response.success) {
                $('#couponDiscount').text('-' + couponAmount.toLocaleString() + '원');
                calculateTotalPrice();
                
                Swal.fire({
                    icon: 'success',
                    title: '쿠폰 적용 완료',
                    text: response.message,
                    confirmButtonColor: '#4E73DF'
                });
            }
        },
        error: function(xhr) {
            var errorMessage = '쿠폰 적용 중 오류가 발생했습니다.';
            if (xhr.responseJSON && xhr.responseJSON.message) {
                errorMessage = xhr.responseJSON.message;
            }
            
            Swal.fire({
                icon: 'error',
                title: '쿠폰 적용 실패',
                text: errorMessage,
                confirmButtonColor: '#4E73DF'
            });
            couponSelect.val('');
            $('#couponDiscount').text('0원');
            calculateTotalPrice();
        }
    });
}

// 쿠폰 취소
function cancelCoupon() {
    $('#couponSelect').val('');
    $('#couponDiscount').text('0원');
    calculateTotalPrice();
    
    // 세션의 쿠폰 사용 정보 제거
    $.ajax({
        url: '/order/cancel-coupon',
        type: 'POST',
        success: function(response) {
            Swal.fire({
                icon: 'success',
                title: '쿠폰 취소 완료',
                text: '쿠폰이 취소되었습니다.',
                confirmButtonColor: '#4E73DF'
            });
        },
        error: function(xhr) {
            Swal.fire({
                icon: 'error',
                title: '쿠폰 취소 실패',
                text: '쿠폰 취소 중 오류가 발생했습니다.',
                confirmButtonColor: '#4E73DF'
            });
        }
    });
}

// 결제 요청
function requestPayment() {
    const selectedPaymentMethod = document.querySelector('input[name="paymentMethod"]:checked');
    
    if (!selectedPaymentMethod) {
        Swal.fire({
            icon: 'warning',
            title: '결제 수단 선택',
            text: '결제 수단을 선택해주세요.',
            confirmButtonColor: '#4E73DF'
        });
        return;
    }
    
    const paymentMethod = selectedPaymentMethod.value;
    
    if (paymentMethod === 'kakaopay') {
        requestPay();
    } else if (paymentMethod === 'card') {
        requestCardPayment();
    }
}

// 카카오페이 결제
function requestPay() {
    const priceInfo = calculateTotalPrice();
    
    IMP.request_pay({
        pg: "kakaopay",
        pay_method: "card",
        merchant_uid: `ORDER-${new Date().getTime()}`,
        name: orderDto.orderName,
        amount: priceInfo.finalPrice,
        buyer_email: orderDto.email,
        buyer_name: orderDto.name,
        buyer_tel: orderDto.phone,
        buyer_addr: orderDto.address
    }, function(rsp) {
        verifyPayment(rsp);
    });
}

// 신용카드 결제
function requestCardPayment() {
    const priceInfo = calculateTotalPrice();
    
    IMP.request_pay({
        pg: "html5_inicis",
        pay_method: "card",
        merchant_uid: `ORDER-${new Date().getTime()}`,
        name: orderDto.orderName,
        amount: priceInfo.finalPrice,
        buyer_email: orderDto.email,
        buyer_name: orderDto.name,
        buyer_tel: orderDto.phone,
        buyer_addr: orderDto.address,
        card_quota: [1,2,3,4,5,6]  // 할부 개월 수 옵션
    }, function(rsp) {
        verifyPayment(rsp);
    });
}

// 결제 검증
function verifyPayment(rsp) {
    console.log("Payment Response:", rsp);
    
    if (!rsp.success) {
        handlePaymentError(rsp.error_msg || '결제에 실패했습니다.');
        return;
    }

    const verificationData = {
        imp_uid: rsp.imp_uid,
        merchant_uid: rsp.merchant_uid,
        amount: rsp.paid_amount
    };

    console.log("Sending verification data:", verificationData);

    $.ajax({
        url: '/orders/verify',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(verificationData),
        success: function(response) {
            if (response.success) {
                Swal.fire({
                    icon: 'success',
                    title: '결제 성공',
                    text: '주문이 완료되었습니다.',
                    confirmButtonColor: '#4E73DF'
                }).then((result) => {
                    if (result.isConfirmed) {
                        location.href = '/order/success/' + response.orderId;
                    }
                });
            } else {
                handlePaymentError(response.message || '결제 검증에 실패했습니다.');
                cancelPayment(rsp.imp_uid, rsp.merchant_uid);
            }
        },
        error: function(xhr) {
            console.error('Verification Error:', xhr.responseJSON);
            handlePaymentError('결제 검증 중 오류가 발생했습니다.');
            cancelPayment(rsp.imp_uid, rsp.merchant_uid);
        }
    });
}

// 결제 오류 처리
function handlePaymentError(message) {
    Swal.fire({
        icon: 'error',
        title: '결제 실패',
        text: message,
        confirmButtonColor: '#4E73DF'
    });
}

// 배송비 안내 모달
function openShippingModal() {
    $("#shippingInfoModal").show();
}

function closeModal() {
    $("#shippingInfoModal").hide();
}

// 모달 외부 클릭 시 닫기
$(window).click(function(e) {
    if (e.target == $("#shippingInfoModal")[0]) {
        $("#shippingInfoModal").hide();
    }
});

// 결제 수단 선택 함수
function selectPaymentMethod(method) {
    // 라디오 버튼 선택
    document.querySelector(`input[value="${method}"]`).checked = true;
    
    // 선택된 스타일 적용
    document.querySelectorAll('.payment_method_option').forEach(option => {
        option.classList.remove('selected');
    });
    document.querySelector(`input[value="${method}"]`).closest('.payment_method_option').classList.add('selected');
}

// 포인트 모달 열기/닫기 함수
function openPointsModal() {
    $("#pointsInfoModal").show();
}

function closePointsModal() {
    $("#pointsInfoModal").hide();
}