package com.workmate.workmate.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Schema(description = "사업장 정보 DTO")
@Getter
@Setter
public class WorkPlaceInfo {
    @Schema(description = "사업장 ID")
    private Long id;

    @Schema(description = "사업장 이름")
    private String name;

    @Schema(description = "사업장 초대 코드")
    private String inviteCode;

    @Schema(description = "사업장 생성 날짜")
    private String createdAt;

    @ArraySchema(schema = @Schema(implementation = UserInfo.class), arraySchema = @Schema(description = "사업장 근무자 리스트"))
    private List<UserInfo> users;

    @ArraySchema(schema = @Schema(implementation = UserInfo.class), arraySchema = @Schema(description = "사업장 관리자 정보"))
    private List<UserInfo> admins;

}
