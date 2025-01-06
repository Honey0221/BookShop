import { searchParams } from './bookMng.js';

export function formatDate(dateString) {
    const date = new Date(dateString);
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
}

export function showAlert(title, icon, text = '') {
    return Swal.fire({
        title: title,
        text: text,
        icon: icon,
        confirmButtonText: '확인'
    });
}

export function createPageItem(page, disabled, iconName) {
    return `
        <li class="page-item ${disabled ? 'disabled' : ''}">
            <a class="page-link" href="#" data-page="${page}">
                <i class="fas fa-${iconName}"></i>
            </a>
        </li>
    `;
}

// 엑셀 다운로드 함수
export function downloadExcel() {
    const downloadUrl = `/admin/items/excel-download?` +
        `searchType=${encodeURIComponent(searchParams.searchType)}` +
        `&keyword=${encodeURIComponent(searchParams.keyword)}` +
        `&status=${encodeURIComponent(searchParams.status)}` +
        `&sort=${encodeURIComponent(searchParams.sort)}`;

    window.location.href = downloadUrl;
}

