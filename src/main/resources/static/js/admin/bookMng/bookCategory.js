export function loadCategories() {
  ['main', 'mid', 'sub', 'detail'].forEach(type => {
    $.get(`/admin/categories/${type}`, function(categories) {
      const select = $(`#${type}Category`);
      select.find('option:gt(0)').remove();
      categories.forEach(category => {
        select.append(`<option value="${category}">${category}</option>`);
      });
    });
  });
}