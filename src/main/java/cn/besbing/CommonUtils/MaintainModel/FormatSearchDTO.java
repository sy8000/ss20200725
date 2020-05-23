package cn.besbing.CommonUtils.MaintainModel;

public class FormatSearchDTO {
	private Integer page;

	private Integer limit;

	private String keyworld;
	
	private String parentId;
	
	private Integer type;



	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public Integer getPage() {
		return page;
	}

	public void setPage(Integer page) {
		this.page = page;
	}

	public Integer getLimit() {
		return limit;
	}

	public void setLimit(Integer limit) {
		this.limit = limit;
	}

	public String getKeyworld() {
		return keyworld;
	}

	public FormatSearchDTO(Integer page, Integer limit, String keyworld) {
		super();
		this.page = page;
		this.limit = limit;
		this.keyworld = keyworld;
	}

	public void setKeyworld(String keyworld) {
		this.keyworld = keyworld;
	}

	public FormatSearchDTO(Integer page, Integer limit, String keyworld, String parentId, Integer type) {
		super();
		this.page = page;
		this.limit = limit;
		this.keyworld = keyworld;
		this.parentId = parentId;
		this.type = type;
	}
	
}
