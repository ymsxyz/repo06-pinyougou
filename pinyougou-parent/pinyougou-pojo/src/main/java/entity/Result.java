package entity;

/*
 	封装返回结果集
 */
public class Result {
	private Boolean success;
	private String message;

	
	public Result() {
		super();
	}
	public Result(Boolean success, String message) {
		super();
		this.success = success;
		this.message = message;
	}
	public Boolean getSuccess() {
		return success;
	}
	public void setSuccess(Boolean success) {
		this.success = success;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
}
