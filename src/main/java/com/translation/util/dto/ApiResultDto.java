package com.translation.util.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResultDto {

	private int ok;
	private int err_no;
	private String failed;
	private String data;

}