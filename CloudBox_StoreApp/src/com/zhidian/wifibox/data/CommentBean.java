package com.zhidian.wifibox.data;


/**
 * 评论列表bean
 * @author zhaoyl
 *
 */
public class CommentBean {

	/**
	 * 状态码（0=请求成功，1=服务器内部错误）
	 */
	public int statusCode;
	/**
	 * 信息描述
	 */
	public String message;
	public int totalPages; //总页数
	public String nickname; //用户名
	public Integer score;  //分数
	public String content; //评论内容
	public String createTime; //发表时间
}
