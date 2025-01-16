export function loadCategories(prefix = 'add') {
  return new Promise((resolve, reject) => {
    ['main', 'mid', 'sub', 'detail'].forEach((type, index, array) => {
      $.get(`/admin/categories/${type}`, function(categories) {
        const select = $(`#${prefix}${type.charAt(0).toUpperCase() + type.slice(1)}Category`);
        select.find('option:gt(0)').remove();
        categories.forEach(category => {
          select.append(`<option value="${category}">${category}</option>`);
        });
        
        // 마지막 카테고리까지 로드되면 resolve
        if (index === array.length - 1) {
          resolve();
        }
      }).fail(reject);
    });
  });
}