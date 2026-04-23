package com.drivereval.regression;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drivereval.common.Constants;
import com.drivereval.common.Result;
import com.drivereval.controller.CommonController;
import com.drivereval.controller.admin.AdminAppealController;
import com.drivereval.controller.admin.AdminUserController;
import com.drivereval.controller.auth.AuthController;
import com.drivereval.controller.driver.DriverEvalController;
import com.drivereval.controller.passenger.PassengerProfileController;
import com.drivereval.entity.Announcement;
import com.drivereval.entity.Appeal;
import com.drivereval.entity.DriverInfo;
import com.drivereval.entity.Evaluation;
import com.drivereval.entity.SysUser;
import com.drivereval.mapper.AnnouncementMapper;
import com.drivereval.mapper.AppealMapper;
import com.drivereval.mapper.ComplaintMapper;
import com.drivereval.mapper.DriverInfoMapper;
import com.drivereval.mapper.DriverPunishMapper;
import com.drivereval.mapper.DriverScoreLogMapper;
import com.drivereval.mapper.EvalTagMapper;
import com.drivereval.mapper.EvaluationMapper;
import com.drivereval.mapper.EvaluationTagRelationMapper;
import com.drivereval.mapper.OrderInfoMapper;
import com.drivereval.mapper.SensitiveWordMapper;
import com.drivereval.mapper.SysUserMapper;
import com.drivereval.mapper.VehicleInfoMapper;
import com.drivereval.mapper.VehicleTypeMapper;
import com.drivereval.service.AppealService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CodeReviewRegressionTest {

    @Test
    void passengerProfileUpdateChangesUsernameWhenUnique() {
        SysUserMapper sysUserMapper = mock(SysUserMapper.class);
        PassengerProfileController controller = new PassengerProfileController();
        ReflectionTestUtils.setField(controller, "sysUserMapper", sysUserMapper);

        SysUser user = new SysUser();
        user.setId(2L);
        user.setUsername("old_name");
        when(sysUserMapper.selectById(2L)).thenReturn(user);
        when(sysUserMapper.selectOne(any())).thenReturn(null);

        Map<String, String> params = new HashMap<>();
        params.put("username", "new_name");
        MockHttpServletRequest request = requestForUser(2L);

        Result<?> result = controller.updateProfile(params, request);

        assertEquals(200, result.getCode());
        assertEquals("new_name", user.getUsername());
        verify(sysUserMapper).updateById(user);
    }

    @Test
    void driverRegistrationCreatesNormalLevelForDefaultEightyScore() {
        SysUserMapper sysUserMapper = mock(SysUserMapper.class);
        DriverInfoMapper driverInfoMapper = mock(DriverInfoMapper.class);
        AuthController controller = new AuthController();
        ReflectionTestUtils.setField(controller, "sysUserMapper", sysUserMapper);
        ReflectionTestUtils.setField(controller, "driverInfoMapper", driverInfoMapper);

        when(sysUserMapper.selectOne(any())).thenReturn(null);
        when(sysUserMapper.insert(any(SysUser.class))).thenAnswer(invocation -> {
            SysUser user = invocation.getArgument(0);
            user.setId(99L);
            return 1;
        });

        Map<String, String> params = new HashMap<>();
        params.put("username", "driver99");
        params.put("password", "123456");
        params.put("role", "DRIVER");

        Result<?> result = controller.register(params);

        assertEquals(200, result.getCode());
        verify(driverInfoMapper).insert(org.mockito.ArgumentMatchers.argThat(info ->
                info.getUserId().equals(99L)
                        && info.getScore().compareTo(new java.math.BigDecimal("80.00")) == 0
                        && info.getLevel().equals(Constants.LEVEL_NORMAL)));
    }

    @Test
    void adminAppealListIncludesUploadedAppealImages() {
        AppealService appealService = mock(AppealService.class);
        ComplaintMapper complaintMapper = mock(ComplaintMapper.class);
        OrderInfoMapper orderInfoMapper = mock(OrderInfoMapper.class);
        DriverInfoMapper driverInfoMapper = mock(DriverInfoMapper.class);
        AdminAppealController controller = new AdminAppealController();
        ReflectionTestUtils.setField(controller, "appealService", appealService);
        ReflectionTestUtils.setField(controller, "complaintMapper", complaintMapper);
        ReflectionTestUtils.setField(controller, "orderInfoMapper", orderInfoMapper);
        ReflectionTestUtils.setField(controller, "driverInfoMapper", driverInfoMapper);

        Appeal appeal = new Appeal();
        appeal.setId(7L);
        appeal.setComplaintId(17L);
        appeal.setDriverId(4L);
        appeal.setContent("申诉内容");
        appeal.setImages("/uploads/a.png,/uploads/b.png");
        Page<Appeal> page = new Page<>(1, 10);
        page.setRecords(Collections.singletonList(appeal));
        page.setTotal(1);
        when(appealService.getAllAppeals(null, new Page<Appeal>(1, 10))).thenReturn(page);
        when(appealService.getAllAppeals(eq(null), any())).thenReturn(page);

        @SuppressWarnings("unchecked")
        Result<Page<Map<String, Object>>> result =
                (Result<Page<Map<String, Object>>>) controller.appealList(null, 1, 10, requestForUser(1L));

        assertEquals(200, result.getCode());
        assertEquals("/uploads/a.png,/uploads/b.png", result.getData().getRecords().get(0).get("images"));
    }

    @Test
    void commonAnnouncementsReturnRequestedPageInsteadOfFullList() {
        AnnouncementMapper announcementMapper = mock(AnnouncementMapper.class);
        CommonController controller = new CommonController();
        ReflectionTestUtils.setField(controller, "announcementMapper", announcementMapper);
        ReflectionTestUtils.setField(controller, "vehicleTypeMapper", mock(VehicleTypeMapper.class));
        ReflectionTestUtils.setField(controller, "sensitiveWordMapper", mock(SensitiveWordMapper.class));
        ReflectionTestUtils.setField(controller, "evalTagMapper", mock(EvalTagMapper.class));

        Page<Announcement> page = new Page<>(2, 5);
        page.setTotal(12);
        when(announcementMapper.selectPage(any(), any())).thenReturn(page);

        @SuppressWarnings("unchecked")
        Result<Page<Announcement>> result =
                (Result<Page<Announcement>>) controller.announcements(null, 2, 5, requestForUser(2L));

        assertEquals(200, result.getCode());
        assertEquals(2, result.getData().getCurrent());
        assertEquals(5, result.getData().getSize());
        verify(announcementMapper, never()).selectList(any());
    }

    @Test
    void adminDeleteRejectsDriverWithLinkedDriverInfo() {
        SysUserMapper sysUserMapper = mock(SysUserMapper.class);
        DriverInfoMapper driverInfoMapper = mock(DriverInfoMapper.class);
        AdminUserController controller = new AdminUserController();
        ReflectionTestUtils.setField(controller, "sysUserMapper", sysUserMapper);
        ReflectionTestUtils.setField(controller, "driverInfoMapper", driverInfoMapper);
        ReflectionTestUtils.setField(controller, "orderInfoMapper", mock(OrderInfoMapper.class));
        ReflectionTestUtils.setField(controller, "vehicleInfoMapper", mock(VehicleInfoMapper.class));

        SysUser driver = new SysUser();
        driver.setId(4L);
        driver.setRole(Constants.ROLE_DRIVER);
        when(sysUserMapper.selectById(4L)).thenReturn(driver);
        when(driverInfoMapper.selectOne(any())).thenReturn(new DriverInfo());

        Result<?> result = controller.deleteUser(4L, requestForUser(1L));

        assertEquals(500, result.getCode());
        assertTrue(result.getMsg().contains("关联"));
        verify(sysUserMapper, never()).deleteById(4L);
    }

    @Test
    void driverStarStatsReturnsMonthlyAverageListForChart() {
        EvaluationMapper evaluationMapper = mock(EvaluationMapper.class);
        DriverEvalController controller = new DriverEvalController();
        ReflectionTestUtils.setField(controller, "evaluationMapper", evaluationMapper);

        Evaluation aprilFiveStar = evaluation(5, LocalDateTime.of(2026, 4, 1, 9, 0));
        Evaluation aprilThreeStar = evaluation(3, LocalDateTime.of(2026, 4, 10, 9, 0));
        Evaluation mayFourStar = evaluation(4, LocalDateTime.of(2026, 5, 1, 9, 0));
        when(evaluationMapper.selectList(any())).thenReturn(Arrays.asList(aprilFiveStar, aprilThreeStar, mayFourStar));

        @SuppressWarnings("unchecked")
        Result<List<Map<String, Object>>> result =
                (Result<List<Map<String, Object>>>) controller.getStarStats(requestForUser(4L));

        assertEquals(200, result.getCode());
        assertEquals(2, result.getData().size());
        assertEquals("2026-04", result.getData().get(0).get("month"));
        assertEquals(4.0, result.getData().get(0).get("avgStar"));
        assertEquals(2, result.getData().get(0).get("count"));
        assertEquals("2026-05", result.getData().get(1).get("month"));
    }

    private MockHttpServletRequest requestForUser(Long userId) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("userId", userId);
        return request;
    }

    private Evaluation evaluation(Integer starRating, LocalDateTime createTime) {
        Evaluation evaluation = new Evaluation();
        evaluation.setStarRating(starRating);
        evaluation.setCreateTime(createTime);
        return evaluation;
    }
}
