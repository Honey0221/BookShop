// IMP 초기화
(function() {
    var IMP = window.IMP;
    IMP.init("imp80047713");
})();

// 카카오페이 결제 처리
function requestPay() {
    const merchantUid = `ORDER-${new Date().getTime()}`;
    
    // 최종 결제 금액 계산
    let finalAmount = orderDto.totalPrice;
    
    const paymentData = {
        pg: "kakaopay.TC0ONETIME",
        pay_method: "card",
        merchant_uid: merchantUid,
        name: orderDto.orderName,
        amount: finalAmount,
        buyer_email: orderDto.email,
        buyer_name: orderDto.name,
        buyer_tel: orderDto.phone,
        buyer_addr: orderDto.address
    };

    window.IMP.request_pay(paymentData, function(rsp) {
        if (rsp.success) {
            verifyPayment(rsp);
        } else {
            Swal.fire({
                icon: 'error',
                title: '결제 실패',
                text: "결제에 실패했습니다. " + rsp.error_msg,
                confirmButtonColor: '#4E73DF'
            });
        }
    });
}

// 일반 카드 결제 처리
function requestCardPayment() {
    const merchantUid = `ORDER-${new Date().getTime()}`;
    
    const paymentData = {
        pg: "nice_v2.iamport03m",
        pay_method: "card",
        merchant_uid: merchantUid,
        name: orderDto.orderName,
        amount: orderDto.totalPrice,
        buyer_email: orderDto.email,
        buyer_name: orderDto.name,
        buyer_tel: orderDto.phone,
        buyer_addr: orderDto.address
    };

    window.IMP.request_pay(paymentData, function(rsp) {
        if (rsp.success) {
            verifyPayment(rsp);
        } else {
            handlePaymentError(rsp);
        }
    });
}

// 결제 검증 및 주문 생성
function verifyPayment(rsp) {
    // 응답 데이터 검증
    console.log("Payment Response:", rsp);
    
    if (!rsp.success) {
        handlePaymentError(rsp.error_msg || '결제에 실패했습니다.');
        return;
    }

    if (!rsp.imp_uid || !rsp.merchant_uid) {
        handlePaymentError('결제 정보가 올바르지 않습니다.');
        return;
    }

    // PaymentDto 필드명과 정확히 일치하도록 수정
    const verificationData = {
        imp_uid: rsp.imp_uid,
        merchant_uid: rsp.merchant_uid,
        amount: rsp.paid_amount
    };

    console.log("Sending verification data:", verificationData);

    // 결제 정보가 준비될 때까지 잠시 대기
    setTimeout(() => {
        // 결제 검증 요청
        $.ajax({
            url: '/orders/verify',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(verificationData),
            success: function(response) {
                console.log("Verification Response:", response);
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
    }, 2000); // 2초 대기
}

// 결제 취소 함수
function cancelPayment(impUid, merchantUid) {
    $.ajax({
        url: '/orders/cancel',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({
            impUid: impUid,
            merchantUid: merchantUid,
            reason: '결제 검증 실패'
        }),
        success: function(response) {
            Swal.fire({
                icon: 'info',
                title: '결제 취소',
                text: '결제가 취소되었습니다.',
                confirmButtonColor: '#4E73DF'
            }).then((result) => {
                if (result.isConfirmed) {
                    location.href = '/cart';
                }
            });
        },
        error: function(xhr) {
            console.error('취소 오류:', xhr.responseJSON);
            Swal.fire({
                icon: 'error',
                title: '결제 취소 실패',
                text: '결제 취소 중 오류가 발생했습니다. 고객센터에 문의해주세요.',
                confirmButtonColor: '#4E73DF'
            });
        }
    });
}

// 결제 에러 처리
function handlePaymentError(message) {
    Swal.fire({
        icon: 'error',
        title: '결제 오류',
        text: message || '결제 처리 중 오류가 발생했습니다.',
        confirmButtonColor: '#4E73DF'
    });
}

// 결제 방식 선택
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

// 결제 수단 선택 UI 처리
function selectPaymentMethod(method) {
    document.querySelectorAll('.payment-method-option').forEach(option => {
        option.classList.remove('selected');
    });
    
    const selectedOption = document.querySelector(`#${method}`).closest('.payment-method-option');
    selectedOption.classList.add('selected');
    
    document.querySelector(`#${method}`).checked = true;
}

document.addEventListener('DOMContentLoaded', function() {
    console.log('OrderDto data:', orderDto);
    calculateTotalWithShipping().catch(console.error);
});

async function calculateTotalWithShipping() {
    // 순수 상품 금액 계산 (이 값은 변경되지 않음)
    const originalPrice = Number(orderDto.totalPrice) || 0;
    const quantity = Number(orderDto.count) || 1;
    
    // 개별 상품 가격 계산 (이 값도 변경되지 않음)
    const unitPrice = Math.floor(originalPrice / quantity);
    
    // 배송비 계산 (비동기)
    const shippingFee = await calculateShippingFee(originalPrice);
    
    // 화면 업데이트
    if (!isNaN(originalPrice)) {
        // 개별 상품 가격 표시 (변경되지 않음)
        document.querySelectorAll('.item-price').forEach(el => {
            el.textContent = unitPrice.toLocaleString() + '원';
        });
        
        // 상품 총액 표시 (순수 상품 금액 - 변경되지 않음)
        document.getElementById('productPrice').textContent = 
            originalPrice.toLocaleString() + '원';
        
        // 배송비 표시
        document.getElementById('deliveryFee').textContent = 
            shippingFee.toLocaleString() + '원';
        
        // 사용된 포인트와 쿠폰 할인액 가져오기 (화면에 표시된 할인 금액 기준)
        const pointDiscountText = document.getElementById('pointDiscount').textContent;
        const couponDiscountText = document.getElementById('couponDiscount').textContent;
        
        // 할인 금액 추출 (- 기호와 '원' 제거 후 숫자만 추출)
        const usedPoints = parseInt(pointDiscountText.replace(/[^0-9]/g, "")) || 0;
        const usedCoupon = parseInt(couponDiscountText.replace(/[^0-9]/g, "")) || 0;
        
        // 최종 금액 계산 및 표시 (순수 상품 금액 + 배송비 - 포인트 - 쿠폰)
        const finalTotal = originalPrice + shippingFee - usedPoints - usedCoupon;
        document.getElementById('finalTotalPrice').textContent = 
            finalTotal.toLocaleString() + '원';
            
        // orderDto의 totalPrice 업데이트 (결제에 사용될 최종 금액)
        orderDto.totalPrice = finalTotal;
    }
}

function showShippingInfo() {
    document.getElementById('shippingInfoModal').style.display = 'block';
}

function closeModal() {
    document.getElementById('shippingInfoModal').style.display = 'none';
}

// 모달 외부 클릭시 닫기
window.onclick = function(event) {
    const modal = document.getElementById('shippingInfoModal');
    if (event.target == modal) {
        modal.style.display = 'none';
    }
}

// Point system functions
async function applyPoint() {
    const pointInput = document.getElementById('usePoint');
    const pointDiscount = document.getElementById('pointDiscount');
    const applyBtn = document.querySelector('.point-apply-btn');
    const cancelBtn = document.querySelector('.point-cancel-btn');
    let points = parseInt(pointInput.value) || 0;
    
    // 현재 상품 금액과 배송비 계산
    const productPrice = parseInt(document.getElementById('productPrice').textContent.replace(/[^0-9]/g, "")) || 0;
    const shippingFee = parseInt(document.getElementById('deliveryFee').textContent.replace(/[^0-9]/g, "")) || 0;
    const couponDiscount = parseInt(document.getElementById('couponDiscount').textContent.replace(/[^0-9]/g, "")) || 0;
    
    // 최종 결제 가능 금액 (상품금액 + 배송비 - 쿠폰할인)
    const maxPayableAmount = productPrice + shippingFee - couponDiscount;
    
    // 포인트가 최종 결제 금액보다 큰 경우
    if (points > maxPayableAmount) {
        Swal.fire({
            icon: 'warning',
            title: '포인트 사용 제한',
            text: `최대 ${maxPayableAmount.toLocaleString()}P까지 사용 가능합니다.`,
            confirmButtonColor: '#4E73DF'
        });
        pointInput.value = 0;
        pointDiscount.textContent = '0원';
        return;
    }
    
    // 포인트가 100단위가 아닌 경우
    if (points % 100 !== 0) {
        Swal.fire({
            icon: 'warning',
            title: '포인트 사용 제한',
            text: '포인트는 100P 단위로만 사용 가능합니다.',
            confirmButtonColor: '#4E73DF'
        });
        pointInput.value = 0;
        pointDiscount.textContent = '0원';
        return;
    }
    
    $.ajax({
        url: '/order/apply-points',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({ points: points }),
        success: function(response) {
            if (response.success) {
                points = response.appliedPoints;
                pointInput.value = points;
                pointInput.disabled = true;
                applyBtn.disabled = true;
                cancelBtn.disabled = false;
                pointDiscount.textContent = (points > 0 ? '-' : '') + points.toLocaleString() + '원';
                calculateTotalWithShipping().catch(console.error);
                Swal.fire({
                    icon: 'success',
                    title: '포인트 적용 완료',
                    text: response.message,
                    confirmButtonColor: '#4E73DF'
                });
            } else {
                Swal.fire({
                    icon: 'error',
                    title: '포인트 적용 실패',
                    text: response.message,
                    confirmButtonColor: '#4E73DF'
                });
                pointInput.value = 0;
                pointDiscount.textContent = '0원';
                calculateTotalWithShipping().catch(console.error);
            }
        },
        error: function(xhr) {
            const response = xhr.responseJSON;
            Swal.fire({
                icon: 'error',
                title: '오류 발생',
                text: response?.message || '포인트 적용 중 오류가 발생했습니다.',
                confirmButtonColor: '#4E73DF'
            });
            pointInput.value = 0;
            pointDiscount.textContent = '0원';
            calculateTotalWithShipping().catch(console.error);
        }
    });
}

// 포인트/쿠폰 취소 시 원래 금액으로 복원하는 함수
async function restoreOriginalPrice() {
    // 현재 화면에 표시된 할인 정보 가져오기
    const productPrice = parseInt(document.getElementById('productPrice').textContent.replace(/[^0-9]/g, "")) || 0;
    const shippingFee = parseInt(document.getElementById('deliveryFee').textContent.replace(/[^0-9]/g, "")) || 0;
    const pointDiscountText = document.getElementById('pointDiscount').textContent;
    const couponDiscountText = document.getElementById('couponDiscount').textContent;
    
    // 할인 금액 추출
    const pointDiscount = parseInt(pointDiscountText.replace(/[^0-9]/g, "")) || 0;
    const couponDiscount = parseInt(couponDiscountText.replace(/[^0-9]/g, "")) || 0;
    
    // 최종 금액 계산 (상품금액 + 배송비 - 포인트할인 - 쿠폰할인)
    const restoredTotal = productPrice + shippingFee - pointDiscount - couponDiscount;
    
    // 화면 업데이트
    document.getElementById('finalTotalPrice').textContent = restoredTotal.toLocaleString() + '원';
    
    // orderDto 업데이트
    orderDto.totalPrice = restoredTotal;
    
    return restoredTotal;
}

function cancelPoint() {
    const pointInput = document.getElementById('usePoint');
    const pointDiscount = document.getElementById('pointDiscount');
    const applyBtn = document.querySelector('.point-apply-btn');
    const cancelBtn = document.querySelector('.point-cancel-btn');

    Swal.fire({
        title: '포인트 취소',
        text: '적용된 포인트를 취소하시겠습니까?',
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#4E73DF',
        cancelButtonColor: '#d33',
        confirmButtonText: '확인',
        cancelButtonText: '취소'
    }).then((result) => {
        if (result.isConfirmed) {
            $.ajax({
                url: '/order/cancel-points',
                type: 'POST',
                contentType: 'application/json',
                success: function(response) {
                    if (response.success) {
                        // 포인트 입력 필드 초기화 및 활성화
                        pointInput.value = '';
                        pointInput.disabled = false;
                        applyBtn.disabled = false;
                        cancelBtn.disabled = true;
                        pointDiscount.textContent = '0원';
                        
                        // 서버에서 받은 업데이트된 금액으로 화면 갱신
                        document.getElementById('finalTotalPrice').textContent = 
                            response.updatedTotalPrice.toLocaleString() + '원';
                        // orderDto 업데이트
                        orderDto.totalPrice = response.updatedTotalPrice;
                        
                        Swal.fire({
                            icon: 'success',
                            title: '포인트 취소 완료',
                            text: '적용된 포인트가 취소되었습니다.',
                            confirmButtonColor: '#4E73DF'
                        });
                    } else {
                        Swal.fire({
                            icon: 'error',
                            title: '포인트 취소 실패',
                            text: response.message || '포인트 취소에 실패했습니다.',
                            confirmButtonColor: '#4E73DF'
                        });
                    }
                },
                error: function(xhr) {
                    const response = xhr.responseJSON;
                    Swal.fire({
                        icon: 'error',
                        title: '오류 발생',
                        text: response?.message || '포인트 취소 중 오류가 발생했습니다.',
                        confirmButtonColor: '#4E73DF'
                    });
                }
            });
        }
    });
}

// Coupon system functions
async function applyCoupon() {
    const productPrice = parseInt($("#productPrice").text().replace(/[^0-9]/g, "")) || 0;
    const selectedCoupon = $("#couponSelect").val();
    const applyCouponBtn = document.getElementById('applyCouponBtn');
    const cancelCouponBtn = document.getElementById('cancelCouponBtn');
    
    if (!selectedCoupon) {
        Swal.fire({
            icon: 'warning',
            title: '쿠폰 선택',
            text: '쿠폰을 선택해주세요.',
            confirmButtonColor: '#4E73DF'
        });
        return;
    }
    
    if (productPrice < 15000) {
        Swal.fire({
            icon: 'warning',
            title: '쿠폰 사용 불가',
            text: '15,000원 이상 구매 시에만 쿠폰을 사용할 수 있습니다.',
            confirmButtonColor: '#4E73DF'
        });
        return;
    }

    $.ajax({
        url: '/order/apply-coupon',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({
            orderAmount: productPrice,
            couponAmount: parseInt(selectedCoupon)
        }),
        success: function(response) {
            if (response.success) {
                const discountAmount = response.discountAmount;
                $("#couponDiscount").text((discountAmount > 0 ? '-' : '') + discountAmount.toLocaleString() + "원");
                $("#couponSelect").prop("disabled", true);
                applyCouponBtn.disabled = true;
                cancelCouponBtn.disabled = false;
                calculateTotalWithShipping().catch(console.error);
                Swal.fire({
                    icon: 'success',
                    title: '쿠폰 적용 완료',
                    text: '쿠폰이 적용되었습니다.',
                    confirmButtonColor: '#4E73DF'
                });
            } else {
                Swal.fire({
                    icon: 'error',
                    title: '쿠폰 적용 실패',
                    text: response.message || "쿠폰 적용에 실패했습니다.",
                    confirmButtonColor: '#4E73DF'
                });
            }
        },
        error: function(xhr) {
            const errorMessage = xhr.responseJSON ? xhr.responseJSON.message : "쿠폰 적용에 실패했습니다.";
            Swal.fire({
                icon: 'error',
                title: '오류 발생',
                text: errorMessage,
                confirmButtonColor: '#4E73DF'
            });
            $("#couponSelect").val("");
        }
    });
}

// 쿠폰 취소
function cancelCoupon() {
    const couponSelect = document.getElementById('couponSelect');
    const applyCouponBtn = document.getElementById('applyCouponBtn');
    const cancelCouponBtn = document.getElementById('cancelCouponBtn');
    
    if (couponSelect.disabled) {
        Swal.fire({
            title: '쿠폰 취소',
            text: '적용된 쿠폰을 취소하시겠습니까?',
            icon: 'question',
            showCancelButton: true,
            confirmButtonColor: '#4E73DF',
            cancelButtonColor: '#d33',
            confirmButtonText: '확인',
            cancelButtonText: '취소'
        }).then((result) => {
            if (result.isConfirmed) {
                $.ajax({
                    url: '/order/cancel-coupon',
                    type: 'POST',
                    contentType: 'application/json',
                    success: function(response) {
                        if (response.success) {
                            couponSelect.value = '';
                            couponSelect.disabled = false;
                            applyCouponBtn.disabled = false;
                            cancelCouponBtn.disabled = true;
                            document.getElementById('couponDiscount').textContent = '0원';
                            
                            // 서버에서 받은 업데이트된 총 금액으로 화면 갱신
                            document.getElementById('finalTotalPrice').textContent = 
                                response.updatedTotalPrice.toLocaleString() + '원';
                            orderDto.totalPrice = response.updatedTotalPrice;
                            
                            Swal.fire({
                                icon: 'success',
                                title: '쿠폰 취소 완료',
                                text: '적용된 쿠폰이 취소되었습니다.',
                                confirmButtonColor: '#4E73DF'
                            });
                        } else {
                            Swal.fire({
                                icon: 'error',
                                title: '쿠폰 취소 실패',
                                text: response.message || '쿠폰 취소에 실패했습니다.',
                                confirmButtonColor: '#4E73DF'
                            });
                        }
                    },
                    error: function(xhr) {
                        const response = xhr.responseJSON;
                        Swal.fire({
                            icon: 'error',
                            title: '오류 발생',
                            text: response?.message || '쿠폰 취소 중 오류가 발생했습니다.',
                            confirmButtonColor: '#4E73DF'
                        });
                    }
                });
            }
        });
    } else {
        Swal.fire({
            icon: 'warning',
            title: '알림',
            text: '적용된 쿠폰이 없습니다.',
            confirmButtonColor: '#4E73DF'
        });
    }
}

// 배송비 계산 로직 수정
function calculateShippingFee(originalPrice) {
    // 먼저 서버에 구독 여부 확인 요청
    return fetch('/subscription/check', {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json'
        }
    })
    .then(response => response.json())
    .then(data => {
        if (data.isSubscriber) {
            // 구독 회원은 무조건 무료 배송
            return 0;
        } else {
            // 비구독 회원은 15,000원 미만일 때만 배송비 부과
            return originalPrice < 15000 ? 3000 : 0;
        }
    })
    .catch(error => {
        console.error('구독 확인 중 오류 발생:', error);
        // 오류 발생 시 기존 로직으로 처리
        return originalPrice < 15000 ? 3000 : 0;
    });
}

// 기존의 배송비 계산 부분을 async/await로 수정
async function updateTotalPrice() {
    const originalPrice = calculateOriginalPrice();
    const shippingFee = await calculateShippingFee(originalPrice);
    const usedPoints = parseInt(document.getElementById('pointInput').value) || 0;
    const couponDiscount = parseInt(document.getElementById('couponDiscountAmount').value) || 0;
    
    const totalPrice = originalPrice + shippingFee - usedPoints - couponDiscount;
    
    document.getElementById('shippingFee').textContent = shippingFee.toLocaleString() + '원';
    document.getElementById('totalPrice').textContent = totalPrice.toLocaleString() + '원';
    
    return {
        originalPrice: originalPrice,
        shippingFee: shippingFee,
        totalPrice: totalPrice
    };
}

// 쿠폰 취소