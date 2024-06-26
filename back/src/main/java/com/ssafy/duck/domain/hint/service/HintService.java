package com.ssafy.duck.domain.hint.service;

import com.ssafy.duck.common.TimeUtil;
import com.ssafy.duck.domain.guest.dto.response.GuestRes;
import com.ssafy.duck.domain.guest.entity.Guest;
import com.ssafy.duck.domain.guest.service.GuestService;
import com.ssafy.duck.domain.hint.dto.request.HintStatusReq;
import com.ssafy.duck.domain.hint.dto.response.HintRes;
import com.ssafy.duck.domain.hint.dto.response.HintStatusRes;
import com.ssafy.duck.domain.hint.entity.Hint;
import com.ssafy.duck.domain.hint.entity.HintStatus;
import com.ssafy.duck.domain.hint.exception.HintErrorCode;
import com.ssafy.duck.domain.hint.exception.HintException;
import com.ssafy.duck.domain.hint.repository.HintRepository;
import com.ssafy.duck.domain.hint.repository.HintStatusRepository;
import com.ssafy.duck.domain.mission.service.MissionService;
import com.ssafy.duck.domain.party.dto.response.PartyRes;
import com.ssafy.duck.domain.party.entity.Party;
import com.ssafy.duck.domain.party.exception.PartyErrorCode;
import com.ssafy.duck.domain.party.exception.PartyException;
import com.ssafy.duck.domain.party.repository.PartyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class HintService {

    private final HintRepository hintRepository;
    private final HintStatusRepository hintStatusRepository;
    private final PartyRepository partyRepository;
    private final GuestService guestService;
    private final MissionService missionService;

    // 힌트 질문 가져오기
    public List<HintRes> getHintQuestion(Long guestId) {
        //힌트 id 가져오기
        List<HintStatus> hintStatusList = hintStatusRepository.findAllByGuestGuestId(guestId);
        if(hintStatusList.isEmpty()){
            throw new HintException(HintErrorCode.STATUS_NOT_FOUND);
        }

        // 힌트를 이미 입력한 경우 hintId:0 반환
        if(hintStatusList.get(0).getHintStatusAnswer() != null){
            System.out.println(hintStatusList.get(0).toString());
            List<HintRes> hintResList = new ArrayList<>();
            HintRes hintRes = HintRes.builder()
                    .hintId(0L)
                    .hintContent("힌트 이미 입력했습니다.")
                    .build();
            hintResList.add(hintRes);
            return hintResList;
        }

        //힌트 질문 가져오기
        List<Hint> hintList = new ArrayList<>();
        for (HintStatus hs : hintStatusList) {
            Hint hint = hintRepository.findById(hs.getHint().getHintId())
                    .orElseThrow(() -> new HintException(HintErrorCode.QUESTION_NOT_FOUND));
            hintList.add(hint);
        }

        List<HintRes> hintResList = hintList.stream().map(HintRes::toDto)
                .collect(Collectors.toList());

        return hintResList;
    }

    public List<Hint> fetch() {
        return hintRepository.findAll();
    }

    // 힌트status에 질문 저장
    public void set(int period, List<Hint> hintList, Long partyId) {
        // 파티아이디로 전체 guest id 가져오기
        List<GuestRes> guestList = guestService.getAllGuestByPartyId(partyId);

        //마니또 기간 계산
        Party party = partyRepository.findByPartyId(partyId)
                .orElseThrow(() -> new PartyException(PartyErrorCode.NOT_FOUND_PARTY));
//        int period = TimeUtil.calcDate(party.getStartTime().toString(), party.getEndTime().toString()) - 1;
        System.out.println("hint period " +period);

        Collections.shuffle(hintList);
        // guest 마다 hint status에 데이터 추가하기
        for (GuestRes guestRes : guestList) {
            HintStatus.HintStatusBuilder hintStatus = HintStatus.builder().guest(Guest.builder().guestId(guestRes.getGuestId()).build());
            for (int i = 0; i < period; i++) {
                hintStatus.hint(hintList.get(i));
                hintStatusRepository.save(hintStatus.build());
            }
        }
    }

    // 힌트 작성하기
    public void setStatus(Long guestId, List<HintStatusReq> hintStatusReq) {
        for (HintStatusReq req : hintStatusReq) {
            HintStatus hintStatus = hintStatusRepository.findByGuestGuestIdAndHintHintId(guestId, req.getHintId());
            if (hintStatus == null)
                throw new HintException(HintErrorCode.STATUS_NOT_FOUND);
            hintStatus.updateAnswer(hintStatus.getHintStatusId(), req.getHintStatusAnswer(), hintStatus.getHint(), hintStatus.getGuest());
            hintStatusRepository.save(hintStatus);
        }
    }

    //힌트 질문+답변 조회
    public List<HintStatusRes> getHintQnA(Long guestId) {
        GuestRes myInfo = guestService.getGuestByUserId(guestId);    // 내 정보
        GuestRes manitoInfo = guestService.findManito(guestId);         // 마니또 정보
        // 마니또 종료 여부 확인
        Party party = partyRepository.findByPartyId(myInfo.getPartyId())
                .orElseThrow(() -> new PartyException(PartyErrorCode.NOT_FOUND_PARTY));
        int isEndSec = TimeUtil.compareDate(TimeUtil.convertToKST(Instant.now()).toString(), party.getEndTime().toString(), 1);
        System.out.println("isEnd Sec " + isEndSec);
        List<HintStatusRes> hintStatusResList = new ArrayList<>();
        List<HintStatus> hintStatusList = hintStatusRepository.findAllByGuestGuestId(manitoInfo.getGuestId());

        // 파티 종료 or 예상 마니또 있는지 확인. 있으면 hint status 전부 가져오기
        if (myInfo.getVotedId() != 0L || isEndSec <1) {
            int fail = missionService.calcMissionFailCount(guestId);
            System.out.println("fail -------------- " + fail);
            for (HintStatus hs : hintStatusList) {
                Hint hint = hintRepository.findById(hs.getHint().getHintId())
                        .orElseThrow(() -> new HintException(HintErrorCode.QUESTION_NOT_FOUND));
                HintStatusRes res = HintStatusRes.builder()
                        .hintContent(hint.getHintContent())
                        .hintId(hint.getHintId())
                        .hintStatusAnswer(hs.getHintStatusAnswer())
                        .build();
                hintStatusResList.add(res);
            }
        }
        // 없으면 mission확인하고 개수만큼 가져오기
        else {
            int hintCount = missionService.calcMissionFailCount(manitoInfo.getGuestId());
            for (int i = 0; i < hintCount; i++) {
                HintStatus eachHs = hintStatusList.get(i);
                Hint hint = hintRepository.findById(eachHs.getHint().getHintId())
                        .orElseThrow(() -> new HintException(HintErrorCode.QUESTION_NOT_FOUND));
                HintStatusRes res = HintStatusRes.builder()
                        .hintContent(hint.getHintContent())
                        .hintId(hint.getHintId())
                        .hintStatusAnswer(eachHs.getHintStatusAnswer())
                        .build();
                hintStatusResList.add(res);
            }
        }


        return hintStatusResList;
    }


}
