import { formatDate, showAlert, createPageItem, downloadExcel } from './utils.js';
import {
    loadBooks,
    handleSearch,
    handleSearchKeypress,
    handleFilterChange,
    handleSortChange,
    handlePageSizeChange
} from './bookList.js';
import {
    handleImagePreview,
    handleNumericInput,
    saveBook,
    resetForm
} from './bookForm.js';

export let searchParams = {
    page: 0,
    size: 10,
    sort: 'id,desc',
    searchType: '',
    keyword: '',
    status: ''
};

$(document).ready(function() {
    // 검색 관련 이벤트
    $('#searchBtn').click(handleSearch);
    $('#searchKeyword').keypress(handleSearchKeypress);
    
    // 필터 관련 이벤트
    $('#statusFilter').change(handleFilterChange);
    $('#sortBy').change(handleSortChange);
    $('#pageSize').change(handlePageSizeChange);
    
    // 이미지 미리보기
    $('#bookImage').change(handleImagePreview);
    
    // 숫자 입력 필드 실시간 검사
    $('#price, #stock').on('input', handleNumericInput);

    // 버튼 클릭 이벤트
    $('#downloadExcelBtn').click(downloadExcel);
    $('#saveBookBtn').click(saveBook);

    // 모달 관련 이벤트
    $('#closeModalBtn').click(resetForm);
    $('#addBookModal').on('hidden.bs.modal', resetForm);
    
    loadBooks();
});
