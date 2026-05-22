package com.bookkeeping.service;

import com.bookkeeping.vo.req.CreateShareReqVO;
import com.bookkeeping.vo.req.ShareCodeReqVO;
import com.bookkeeping.vo.resp.ShareRespVO;

public interface ShareService {

    ShareRespVO createShare(Long userId, CreateShareReqVO reqVO);

    ShareRespVO getShareInfo(String shareCode);

    void acceptShare(Long userId, String shareCode);
}
