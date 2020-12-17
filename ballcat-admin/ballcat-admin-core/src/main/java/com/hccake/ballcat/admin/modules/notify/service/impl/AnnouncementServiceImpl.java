package com.hccake.ballcat.admin.modules.notify.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.hccake.ballcat.admin.constants.AnnouncementStatusEnum;
import com.hccake.ballcat.admin.modules.notify.event.AnnouncementPublishEvent;
import com.hccake.ballcat.admin.modules.notify.mapper.AnnouncementMapper;
import com.hccake.ballcat.admin.modules.notify.model.converter.AnnouncementConverter;
import com.hccake.ballcat.admin.modules.notify.model.dto.AnnouncementDTO;
import com.hccake.ballcat.admin.modules.notify.model.entity.Announcement;
import com.hccake.ballcat.admin.modules.notify.model.qo.AnnouncementQO;
import com.hccake.ballcat.admin.modules.notify.model.vo.AnnouncementVO;
import com.hccake.ballcat.admin.modules.notify.service.AnnouncementService;
import com.hccake.ballcat.common.core.exception.BusinessException;
import com.hccake.ballcat.common.core.result.SystemResultCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * 公告信息
 *
 * @author hccake 2020-12-15 17:01:15
 */
@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl extends ServiceImpl<AnnouncementMapper, Announcement>
		implements AnnouncementService {

	private final ApplicationEventPublisher publisher;

	private final static String TABLE_ALIAS_PREFIX = "a.";

	/**
	 * 根据QueryObeject查询分页数据
	 * @param page 分页参数
	 * @param qo 查询参数对象
	 * @return IPage<AnnouncementVO> 分页数据
	 */
	@Override
	public IPage<AnnouncementVO> selectPageVo(IPage<?> page, AnnouncementQO qo) {
		QueryWrapper<Announcement> wrapper = Wrappers.<Announcement>query()
				.like(StrUtil.isNotBlank(qo.getTitle()), TABLE_ALIAS_PREFIX + "title", qo.getTitle())
				.in(qo.getStatus() != null && qo.getStatus().length > 0, TABLE_ALIAS_PREFIX + "status", qo.getStatus())
				.eq(ObjectUtil.isNotNull(qo.getRecipientFilterType()), TABLE_ALIAS_PREFIX + "recipient_filter_type",
						qo.getRecipientFilterType());
		return baseMapper.selectPageVo(page, wrapper);
	}

	/**
	 * 创建公告
	 * @param announcementDTO 公告信息
	 * @return boolean
	 */
	@Override
	public boolean addAnnouncement(AnnouncementDTO announcementDTO) {
		Announcement announcement = AnnouncementConverter.INSTANCE.dtoToPo(announcementDTO);
		announcement.setId(null);
		int flag = baseMapper.insert(announcement);
		boolean inserted = SqlHelper.retBool(flag);
		// 公告发布事件
		boolean isPublishStatus = announcement.getStatus() == AnnouncementStatusEnum.ENABLED.getValue();
		if (inserted && isPublishStatus) {
			publisher.publishEvent(new AnnouncementPublishEvent(announcement));
		}
		return inserted;
	}

	/**
	 * 更新公告信息
	 * @param announcementDTO announcementDTO
	 * @return boolean
	 */
	@Override
	public boolean updateAnnouncement(AnnouncementDTO announcementDTO) {
		Announcement oldAnnouncement = baseMapper.selectById(announcementDTO.getId());
		if (oldAnnouncement.getStatus() != AnnouncementStatusEnum.UNPUBLISHED.getValue()) {
			throw new BusinessException(SystemResultCode.BAD_REQUEST.getCode(), "不允许修改已经发布过的公告！");
		}

		Announcement announcement = AnnouncementConverter.INSTANCE.dtoToPo(announcementDTO);
		// 不允许修改为《发布中》以外的状态
		boolean isPublishStatus = announcement.getStatus() == AnnouncementStatusEnum.ENABLED.getValue();
		if (!isPublishStatus) {
			announcement.setStatus(null);
		}
		// 保证当前状态未被修改过
		int flag = baseMapper.update(announcement,
				Wrappers.<Announcement>lambdaUpdate().eq(Announcement::getId, announcement.getId())
						.eq(Announcement::getStatus, AnnouncementStatusEnum.UNPUBLISHED.getValue()));
		boolean isUpdated = SqlHelper.retBool(flag);

		// 公告发布事件
		if (isUpdated && isPublishStatus) {
			publisher.publishEvent(new AnnouncementPublishEvent(announcement));
		}
		return isUpdated;
	}

	/**
	 * 发布公告信息
	 * @param announcementId 公告ID
	 * @return boolean
	 */
	@Override
	public boolean publish(Long announcementId) {
		Announcement oldAnnouncement = baseMapper.selectById(announcementId);
		if (oldAnnouncement.getStatus() != AnnouncementStatusEnum.UNPUBLISHED.getValue()) {
			throw new BusinessException(SystemResultCode.BAD_REQUEST.getCode(), "不允许修改已经发布过的公告！");
		}

		Announcement announcement = new Announcement();
		announcement.setId(announcementId);
		announcement.setStatus(AnnouncementStatusEnum.ENABLED.getValue());
		int flag = baseMapper.update(announcement,
				Wrappers.<Announcement>lambdaUpdate().eq(Announcement::getId, announcement.getId())
						.eq(Announcement::getStatus, AnnouncementStatusEnum.UNPUBLISHED.getValue()));
		boolean isUpdated = SqlHelper.retBool(flag);
		if (isUpdated) {
			oldAnnouncement.setStatus(AnnouncementStatusEnum.ENABLED.getValue());
			publisher.publishEvent(new AnnouncementPublishEvent(oldAnnouncement));
		}
		return isUpdated;
	}

	/**
	 * 关闭公告信息
	 * @param announcementId 公告ID
	 * @return boolean
	 */
	@Override
	public boolean close(Long announcementId) {
		Announcement announcement = new Announcement();
		announcement.setId(announcementId);
		announcement.setStatus(AnnouncementStatusEnum.DISABLED.getValue());
		int flag = baseMapper.updateById(announcement);
		return SqlHelper.retBool(flag);
	}

}
