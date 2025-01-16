import { showAlert } from './utils.js';
import { loadBooks } from './bookList.js';
import { loadCategories } from './bookCategory.js';

export function createBookFormDto(formId) {
  const prefix = formId === '#editBookForm' ? 'edit' : 'add';
  return {
    title: $(`#${prefix}Title`).val().trim(),
    author: $(`#${prefix}Author`).val().trim(),
    publisher: $(`#${prefix}Publisher`).val().trim(),
    price: parseInt($(`#${prefix}Price`).val()),
    stock: parseInt($(`#${prefix}Stock`).val()),
    mainCategory: $(`#${prefix}MainCategory`).val(),
    midCategory: $(`#${prefix}MidCategory`).val() || '',
    subCategory: $(`#${prefix}SubCategory`).val() || '',
    detailCategory: $(`#${prefix}DetailCategory`).val() || '',
    description: $(`#${prefix}Description`).val().trim()
  };
}

// 도서 저장 관련 함수들
export function saveBook() {
  const isEdit = $(this).attr('id') === 'updateBookBtn';
  const modalId = isEdit ? '#editBookModal' : '#addBookModal';
  const formId = isEdit ? '#editBookForm' : '#addBookForm';
  const prefix = formId === '#editBookForm' ? 'edit' : 'add';

  if (!validateBookForm(formId)) {
      return;
  }

  const formData = new FormData();
  const bookFormDto = createBookFormDto(formId);
  const bookId = isEdit ? $('#editBookId').val() : null;

  formData.append('bookFormDto', new Blob([JSON.stringify(bookFormDto)], {
    type: 'application/json'
  }));

  const bookImage = $(`${formId} #${prefix}BookImage`)[0].files[0];
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
      $(modalId).modal('hide');
      showAlert(isEdit ? '수정 완료' : '저장 완료', 'success',
                          `도서가 성공적으로 ${isEdit ? '수정' : '추가'}되었습니다.`
      ).then((result) => {
        if (result.isConfirmed) {
            loadBooks();
        }
      });
      resetForm(formId);
    },
    error: function() {
      showAlert(isEdit ? '수정 실패' : '저장 실패', 'error',
                         `도서 ${isEdit ? '수정' : '추가'} 중 오류가 발생했습니다.`);
    }
  });
}

function validateBookForm(formId) {
    const prefix = formId === '#editBookForm' ? 'edit' : 'add';
    const requiredFields = [
        { id: `${prefix}Title`, name: '제목' },
        { id: `${prefix}Author`, name: '저자' },
        { id: `${prefix}Publisher`, name: '출판사' },
        { id: `${prefix}MainCategory`, name: '대분류' },
        { id: `${prefix}MidCategory`, name: '중분류' },
        { id: `${prefix}SubCategory`, name: '소분류' }
    ];

    for (const field of requiredFields) {
        if (!validateField(field)) {
            return false;
        }
    }

    if (!validateNumericField('Price', '가격', formId) || !validateNumericField('Stock', '재고', formId)) {
        return false;
    }

    const isEdit = formId === '#editBookForm';
    if (!isEdit && !validateImageFile(formId)) {
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

function validateNumericField(fieldId, fieldName, formId = '#addBookForm') {
    const prefix = formId === '#editBookForm' ? 'edit' : 'add';
    const value = parseInt($(`#${prefix}${fieldId}`).val());
    if (isNaN(value) || value < 0) {
        showAlert('입력 오류', 'warning', `${fieldName}은(는) 0 이상의 숫자로 입력해주세요.`);
        $(`#${prefix}${fieldId}`).focus();
        return false;
    }
    return true;
}

function validateImageFile(formId) {
    const prefix = formId === '#editBookForm' ? 'edit' : 'add';
    const imageFile = $(`${formId} #${prefix}BookImage`)[0].files[0];
    const isEdit = formId === '#editBookForm';

    // 수정 시에는 이미지 필수 아님
    if (!isEdit && !imageFile) {
        showAlert('입력 오류', 'warning', '도서 이미지를 선택해주세요.');
        return false;
    }
    return true;
}

export function handleImagePreview() {
    const formId = $(this).closest('form').attr('id');
    const prefix = formId === 'editBookForm' ? 'edit' : 'add';
    const file = this.files[0];
    if (file) {
        const reader = new FileReader();
        reader.onload = function(e) {
            $(`#${prefix}ImagePreview`)
            .html(`<img src="${e.target.result}" class="img-fluid" alt="미리보기">`);
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
export function resetForm(formId = '#addBookForm') {
    const prefix = formId === '#editBookForm' ? 'edit' : 'add';
    $(formId)[0].reset();
    $(`#${prefix}ImagePreview`).empty();
    $(`#${prefix}MainCategory`).val('');
    $(`#${prefix}MidCategory`).val('');
    $(`#${prefix}SubCategory`).val('');
    $(`#${prefix}DetailCategory`).val('');

    if (formId === '#editBookForm') {
        $('#editBookId').val('');
    }

    loadCategories(prefix);
}