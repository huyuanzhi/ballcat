package com.hccake.ballcat.admin.modules.notify.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hccake.ballcat.admin.modules.notify.model.dto.AnnouncementDTO;
import com.hccake.ballcat.admin.modules.notify.model.qo.AnnouncementQO;
import com.hccake.ballcat.admin.modules.notify.model.vo.AnnouncementVO;
import com.hccake.ballcat.admin.modules.notify.service.AnnouncementService;
import com.hccake.ballcat.commom.log.operation.annotation.CreateOperationLogging;
import com.hccake.ballcat.commom.log.operation.annotation.DeleteOperationLogging;
import com.hccake.ballcat.commom.log.operation.annotation.UpdateOperationLogging;
import com.hccake.ballcat.common.core.result.BaseResultCode;
import com.hccake.ballcat.common.core.result.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 公告信息
 *
 * @author hccake 2020-12-15 17:01:15
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/notify/announcement")
@Api(value = "announcement", tags = "公告信息管理")
public class AnnouncementController {

	private final AnnouncementService announcementService;

	/**
	 * 分页查询
	 * @param page 分页对象
	 * @param announcementQO 公告信息查询对象
	 * @return R 通用返回体
	 */
	@ApiOperation(value = "分页查询", notes = "分页查询")
	@GetMapping("/page")
	@PreAuthorize("@per.hasPermission('notify:announcement:read')")
	public R<IPage<AnnouncementVO>> getAnnouncementPage(Page<?> page, AnnouncementQO announcementQO) {
		return R.ok(announcementService.selectPageVo(page, announcementQO));
	}

	/**
	 * 新增公告信息
	 * @param announcementDTO 公告信息
	 * @return R 通用返回体
	 */
	@ApiOperation(value = "新增公告信息", notes = "新增公告信息")
	@CreateOperationLogging(msg = "新增公告信息")
	@PostMapping
	@PreAuthorize("@per.hasPermission('notify:announcement:add')")
	public R<?> save(@Valid @RequestBody AnnouncementDTO announcementDTO) {
		return announcementService.addAnnouncement(announcementDTO) ? R.ok()
				: R.failed(BaseResultCode.UPDATE_DATABASE_ERROR, "新增公告信息失败");
	}

	/**
	 * 修改公告信息
	 * @param announcementDTO 公告信息
	 * @return R 通用返回体
	 */
	@ApiOperation(value = "修改公告信息", notes = "修改公告信息")
	@UpdateOperationLogging(msg = "修改公告信息")
	@PutMapping
	@PreAuthorize("@per.hasPermission('notify:announcement:edit')")
	public R<?> updateById(@Valid @RequestBody AnnouncementDTO announcementDTO) {
		return announcementService.updateAnnouncement(announcementDTO) ? R.ok()
				: R.failed(BaseResultCode.UPDATE_DATABASE_ERROR, "修改公告信息失败");
	}

	/**
	 * 通过id删除公告信息
	 * @param id id
	 * @return R 通用返回体
	 */
	@ApiOperation(value = "通过id删除公告信息", notes = "通过id删除公告信息")
	@DeleteOperationLogging(msg = "通过id删除公告信息")
	@DeleteMapping("/{id}")
	@PreAuthorize("@per.hasPermission('notify:announcement:del')")
	public R<?> removeById(@PathVariable Long id) {
		return announcementService.removeById(id) ? R.ok()
				: R.failed(BaseResultCode.UPDATE_DATABASE_ERROR, "通过id删除公告信息失败");
	}

	/**
	 * 发布公告信息
	 * @return R 通用返回体
	 */
	@ApiOperation(value = "发布公告信息", notes = "发布公告信息")
	@UpdateOperationLogging(msg = "发布公告信息")
	@PatchMapping("/publish/{announcementId}")
	@PreAuthorize("@per.hasPermission('notify:announcement:edit')")
	public R<?> enableAnnouncement(@PathVariable Long announcementId) {
		return announcementService.publish(announcementId) ? R.ok()
				: R.failed(BaseResultCode.UPDATE_DATABASE_ERROR, "发布公告信息失败");
	}

	/**
	 * 关闭公告信息
	 * @return R 通用返回体
	 */
	@ApiOperation(value = "关闭公告信息", notes = "关闭公告信息")
	@UpdateOperationLogging(msg = "关闭公告信息")
	@PatchMapping("/close/{announcementId}")
	@PreAuthorize("@per.hasPermission('notify:announcement:edit')")
	public R<?> disableAnnouncement(@PathVariable Long announcementId) {
		return announcementService.close(announcementId) ? R.ok()
				: R.failed(BaseResultCode.UPDATE_DATABASE_ERROR, "关闭公告信息失败");
	}

}