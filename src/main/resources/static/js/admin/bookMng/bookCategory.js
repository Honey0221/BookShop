// 카테고리 관련 함수들
export function loadMidCategories() {
    loadCategories('mid', '#midCategory');
}

export function loadSubCategories() {
    loadCategories('sub', '#subCategory');
}

export function loadDetailCategories() {
    loadCategories('detail', '#detailCategory');
}

export function loadCategories(type, selector) {
  $.get(`/admin/categories/${type}`, function(categories) {
    const select = $(`#${type}Category`);
    select.find('option:gt(0)').remove();
    categories.forEach(category => {
      select.append(`<option value="${category}">${category}</option>`);
    });
    if (selector) {
      select.val(selector);
    }
  });
}