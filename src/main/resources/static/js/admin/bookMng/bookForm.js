import { showAlert } from './utils.js';
import { loadBooks } from './bookList.js';

export function createBookFormDto() {
    return {
        title: $('#title').val().trim(),
        author: $('#author').val().trim(),
        publisher: $('#publisher').val().trim(),
        price: parseInt($('#price').val()),
        stock: parseInt($('#stock').val()),
        mainCategory: $('#mainCategory').val(),
        midCategory: $('#midCategory').val() || '',
        subCategory: $('#subCategory').val() || '',
        detailCategory: $('#detailCategory').val() || '',
        description: $('#description').val().trim()
    };
}

// 도서 저장 관련 함수들
export function saveBook() {
  if (!validateBookForm()) {
      return;
  }

  const formData = new FormData();
  const bookFormDto = createBookFormDto();
  const bookId = $('#saveBookBtn').data('id');
  const isEdit = !!bookId;

  formData.append('bookFormDto', new Blob([JSON.stringify(bookFormDto)], {
    type: 'application/json'
  }));

  const bookImage = $('#bookImage')[0].files[0];
  if (bookImage) {
    formData.append('bookImage', bookImage);
  }

  $.ajax({
    url: isEdit ? `/admin/items/${bookId}` : '/admin/items/new',
    type: isEdit ? 'PUT' : 'POST',
    data: formData,
    processData: false,
    contentType: false,
    success: function(response) {
      $('#addBookModal').modal('hide');
      showAlert(isEdit ? '수정 완료' : '저장 완료', 'success',
                          `도서가 성공적으로 ${isEdit ? '수정' : '추가'}되었습니다.`
      ).then((result) => {
        if (result.isConfirmed) {
            loadBooks();
        }
      });
      resetForm();
    },
    error: function() {
      showAlert(isEdit ? '수정 실패' : '저장 실패', 'error',
                         `도서가 ${isEdit ? '수정' : '추가'} 중 오류가 발생했습니다.`);
    }
  });
}

function validateBookForm() {
    const requiredFields = [
        { id: 'title', name: '제목' },
        { id: 'author', name: '저자' },
        { id: 'publisher', name: '출판사' },
        { id: 'mainCategory', name: '대분류' },
        { id: 'midCategory', name: '중분류' },
        { id: 'subCategory', name: '소분류' }
    ];

    for (const field of requiredFields) {
        if (!validateField(field)) {
            return false;
        }
    }

    if (!validateNumericField('price', '가격') || !validateNumericField('stock', '재고')) {
        return false;
    }

    const isEdit = !!$('#saveBookBtn').data('id');
    if (!isEdit && !validateImageFile()) {
        return false;
    }

    return true;
}

// 유효성 검사 관련 함수들
function validateField(field) {
    const value = $(`#${field.id}`).val();
    if (!value || value.trim() === '') {
        showAlert('입력 오류', 'warning', `${field.name}을(를) 입력해주세요.`);
        $(`#${field.id}`).focus();
        return false;
    }
    return true;
}

function validateNumericField(fieldId, fieldName) {
    const value = parseInt($(`#${fieldId}`).val());
    if (isNaN(value) || value < 0) {
        showAlert('입력 오류', 'warning', `${fieldName}은(는) 0 이상의 숫자를 입력해주세요.`);
        $(`#${fieldId}`).focus();
        return false;
    }
    return true;
}

function validateImageFile() {
    const imageFile = $('#bookImage')[0].files[0];
    if (!imageFile) {
        showAlert('입력 오류', 'warning', '도서 이미지를 선택해주세요.');
        return false;
    }
    return true;
}

export function handleImagePreview() {
    const file = this.files[0];
    if (file) {
        const reader = new FileReader();
        reader.onload = function(e) {
            $('#imagePreview').html(`<img src="${e.target.result}" class="img-fluid" alt="미리보기">`);
        };
        reader.readAsDataURL(file);
    }
}

export function handleNumericInput() {
    const value = $(this).val();
    if (value && !/^\d*$/.test(value)) {
        $(this).val(value.replace(/[^\d]/g, ''));
    }
}

// 폼 초기화 함수
export function resetForm() {
    $('#addBookForm')[0].reset();
    $('#imagePreview').empty();
    $('#midCategory').val('');
    $('#subCategory').val('');
    $('#detailCategory').val('');
    $('#addBookModalLabel').text('도서 추가');
    $('#saveBookBtn').removeData('id');
}