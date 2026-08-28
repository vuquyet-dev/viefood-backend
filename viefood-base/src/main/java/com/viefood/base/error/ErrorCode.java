package com.viefood.base.error;

public enum ErrorCode {
    ERR_FORMAT_REQUEST(400, "Dữ liệu gửi lên không hợp lệ"),
    ERR_INVALID_DATA(400, "Dữ liệu không đạt yêu cầu kiểm tra"),
    ERR_AUTHEN_FAIL(401, "Bạn cần đăng nhập"),
    ERR_INVALID_TOKEN(401, "Token không hợp lệ hoặc đã hết hạn"),
    ERR_INVALID_REF_TOKEN(401, "Refresh token không hợp lệ"),
    ERR_ACCESS_DENIED(403, "Bạn không có quyền thực hiện thao tác này"),
    ERR_DATA_NOT_FOUND(404, "Không tìm thấy dữ liệu yêu cầu"),
    ERR_DATA_DUPLICATE(409, "Dữ liệu đã tồn tại hoặc bị xung đột"),
    ERR_REQUEST_TOO_LARGE(413, "File hoặc request vượt dung lượng cho phép"),
    ERR_UNSUPPORTED_TYPE(415, "Định dạng file không được hỗ trợ"),
    ERR_INVALID_OPERATION(422, "Thao tác vi phạm quy tắc nghiệp vụ"),
    ERR_TOO_MANY_REQUESTS(429, "Bạn gửi yêu cầu quá nhanh"),
    ERR_INTERNAL_ERROR(500, "Hệ thống gặp lỗi không mong muốn"),
    ERR_UPSTREAM_ERROR(502, "Dịch vụ phụ trợ trả về lỗi"),
    ERR_SERVICE_UNAVAILABLE(503, "Dịch vụ hiện chưa sẵn sàng"),
    ERR_REQUEST_TIMEOUT(504, "Quá thời gian request");



    private final int httpStatus;
    private final String defaultMessage;

    ErrorCode(int httpStatus, String defaultMessage) {
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
