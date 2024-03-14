package com.ssafy.duck.domain.mission.service;

import com.ssafy.duck.domain.guest.dto.response.GuestRes;
import com.ssafy.duck.domain.guest.entity.Guest;
import com.ssafy.duck.domain.guest.service.GuestService;
import com.ssafy.duck.domain.mission.dto.response.MissionResultRes;
import com.ssafy.duck.domain.mission.entity.Mission;
import com.ssafy.duck.domain.mission.entity.MissionStatus;
import com.ssafy.duck.domain.mission.repository.MissionRepository;
import com.ssafy.duck.domain.mission.repository.MissionStatusRepository;
import com.ssafy.duck.domain.party.service.PartyService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Transactional
@RequiredArgsConstructor
public class MissionService {
    @Autowired
    private final MissionRepository missionRepository;
    @Autowired
    private final MissionStatusRepository missionStatusRepository;
    @Autowired
    private GuestService guestService;


    //종료시간과 현재시간 비교해서 날짜개수만큼 랜덤으로 미션 가져오기
    public List<List<Long>> fetch(Instant endTime) {
        Instant current = Instant.now();

        //day 수 구하기
        int curLocalTime = LocalDateTime.ofInstant(current, ZoneId.systemDefault()).getDayOfMonth();
        int endLocalTime = LocalDateTime.ofInstant(endTime, ZoneId.systemDefault()).getDayOfMonth();
        int period = endLocalTime - curLocalTime;

        //미션 개수 가져오기
        long totalCount = missionRepository.count();
        List<Long> totalIndex = new ArrayList<>();
        for (long i = 1; i <= totalCount; i++) {
            totalIndex.add(i);
        }

        List<List<Long>> selectedList = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            List<Long> eachMissionList = new ArrayList<>();
            for (long j = 0; j < period; j++) {
                int random = ThreadLocalRandom.current().nextInt(totalIndex.size());
                eachMissionList.add(totalIndex.remove(random));
            }
            selectedList.add(eachMissionList);
        }

        System.out.println("--- mission selected" + selectedList);

        return selectedList;
    }

    // 미션 status에 질문 저장
    public void set(List<List<Long>> indexList, Long partyId) {
        // 파티아이디로 전체 guest id 가져오기
        List<GuestRes> guestList = guestService.getAllGuest(partyId);
//        System.out.println("--- hint guest List ");
//        for (GuestRes guestRes : guestList) {
//            System.out.println(guestRes.getGuestId());
//        }

        // guest 마다 mission status에 데이터 추가하기
        // indexList : 4 x Day
        // [[day1-1 day2-1 day3-1], [day1-2 day2-2 day3-2] ... ] -> [[12, 21, 20], [5, 15, 4], [10, 2, 8], [9, 3, 19]]
        for (GuestRes guestRes : guestList) {
            for (List<Long> eachIndex : indexList) {
                // 한국 시간으로 현재 날짜 00시
                Instant getTimeInstant = Instant.now().atZone(ZoneId.of("+9")).toLocalDate().atStartOfDay().toInstant(ZoneOffset.UTC);
                for (Long index : eachIndex) {
//                    System.out.println("guest " + guestRes.getGuestId() + " / index " + index + " / cur " + getTimeInstant);
                    MissionStatus missionStatus = MissionStatus.builder()
                            .guest(Guest.builder().guestId(guestRes.getGuestId()).build())
                            .mission(Mission.builder().missionId(index).build())
                            .getTime(getTimeInstant)
                            .build();
                    missionStatusRepository.save(missionStatus);
                    // 다음 날의 00:00:00을 찾기
                    getTimeInstant = getTimeInstant.plus(Duration.ofDays(1));
                }
            }
        }
    }


    // hint에서 사용 : 마니또의 미션 수행 실패 개수 반환
    // 실패 개수 = 어제까지 미션 status 조회 후 전체 개수/3 - 성공 개수
    public int calcMissionFailCount(Long manitoId) {
        // 한국 시간으로 현재 날짜 00시
        Instant today = Instant.now().atZone(ZoneId.of("+9")).toLocalDate().atStartOfDay().toInstant(ZoneOffset.UTC);
//        today = today.plus(Duration.ofDays(3)); // 테스트를 위해 오늘 날짜 변경
        System.out.println("calcMissionFailCount " + today);

        List<MissionStatus> missionStatusList = missionStatusRepository.findAllByGuestGuestIdAndGetTimeBefore(manitoId, today);
        int failCount = missionStatusList.size() / 4; // 어제까지의 미션 개수
        for (MissionStatus ms : missionStatusList) {
            System.out.println(ms.getMissionStatusId() + " / " + ms.getGetTime() + " / " + ms.getSuccessTime());
            if (ms.getSuccessTime() != null)
                failCount--;
        }
        System.out.println("failCount " + failCount);
        return failCount;
    }

    // 내 미션 수행 반환
    // 수행한 미션 : imageUrl / 실패한 미션 : null
    public List<MissionResultRes> getMyMissionResult(Long guestId) {
        System.out.println("getMyMissionResult");

        List<MissionStatus> missionStatusList = missionStatusRepository.findByGuestGuestIdOrderByGetTime(guestId);
        List<MissionResultRes> missionResultResList = new ArrayList<>();
        // 미션을 날짜별로 확인 : 총 4개
        // day1 : successtime이 있으면 1순위, 없으면 confirmdate가 있으면서 id가 큰 순서 2순위, confirmdate가 없으면 id가 가장 작은거
        for (int i = 0; i < missionStatusList.size(); i += 4) {
            MissionResultRes.MissionResultResBuilder mrRes = MissionResultRes.builder().guestId(guestId);

            for (int j = i; j < i + 4; j++) {
                MissionStatus ms = missionStatusList.get(j);
                if (ms.getSuccessTime() != null) {
                    mrRes.missionStatusId(ms.getMissionStatusId())
                            .missionContent(ms.getMission().getMissionContent())
                            .missionImageUrl(ms.getMissionImageUrl())
                            .getTime(ms.getGetTime())
                            .successTime(ms.getSuccessTime())
                            .confirmTime(ms.getConfirmTime());
                    break;
                } else if (ms.getConfirmTime() != null) {
                    if (mrRes.build().getMissionStatusId() == null) {
                        mrRes.missionStatusId(ms.getMissionStatusId())
                                .missionContent(ms.getMission().getMissionContent())
                                .getTime(ms.getGetTime())
                                .successTime(ms.getSuccessTime())
                                .confirmTime(ms.getConfirmTime());
                    } else {
                        mrRes.missionStatusId(ms.getMissionStatusId() > mrRes.build().getMissionStatusId() ? ms.getMissionStatusId() : mrRes.build().getMissionStatusId())
                                .getTime(ms.getGetTime())
                                .successTime(ms.getSuccessTime())
                                .confirmTime(ms.getConfirmTime());
                    }
                } else {
                    if (mrRes.build().getConfirmTime() != null) continue;

                    mrRes.missionStatusId(ms.getMissionStatusId())
                            .missionContent(ms.getMission().getMissionContent())
                            .getTime(ms.getGetTime())
                            .successTime(ms.getSuccessTime())
                            .confirmTime(ms.getConfirmTime());
                    break;
                }
            }
            missionResultResList.add(mrRes.build());
        }
//        for (MissionResultRes mrRes : missionResultResList) {
//            System.out.println(mrRes.toString());
//        }
        return missionResultResList;
    }


    // 마니또의 미션 수행 반환
    // 수행한 미션 : imageUrl / 실패한 미션 : null
    public List<MissionResultRes> getManitoMissionResult(Long guestId) {
        GuestRes manito = guestService.findManito(guestId);         // 마니또 정보

        List<MissionStatus> missionStatusList = missionStatusRepository.findByGuestGuestIdOrderByGetTime(manito.getGuestId());
        List<MissionResultRes> missionResultResList = new ArrayList<>();

        for (int i = 0; i < missionStatusList.size(); i += 4) {
            MissionResultRes.MissionResultResBuilder mrRes = MissionResultRes.builder().guestId(manito.getGuestId());

            for (int j = i; j < i + 4; j++) {
                MissionStatus ms = missionStatusList.get(j);
                if (ms.getSuccessTime() != null) {
                    mrRes.missionStatusId(ms.getMissionStatusId())
                            .missionContent(ms.getMission().getMissionContent())
                            .missionImageUrl(ms.getMissionImageUrl())
                            .getTime(ms.getGetTime())
                            .successTime(ms.getSuccessTime())
                            .confirmTime(ms.getConfirmTime());
                    break;
                } else if (ms.getConfirmTime() != null) {
                    if (mrRes.build().getMissionStatusId() == null) {
                        mrRes.missionStatusId(ms.getMissionStatusId())
                                .missionContent(ms.getMission().getMissionContent())
                                .getTime(ms.getGetTime())
                                .successTime(ms.getSuccessTime())
                                .confirmTime(ms.getConfirmTime());
                    } else {
                        mrRes.missionStatusId(ms.getMissionStatusId() > mrRes.build().getMissionStatusId() ? ms.getMissionStatusId() : mrRes.build().getMissionStatusId())
                                .getTime(ms.getGetTime())
                                .successTime(ms.getSuccessTime())
                                .confirmTime(ms.getConfirmTime());
                    }
                } else {
                    if (mrRes.build().getConfirmTime() != null) continue;

                    mrRes.missionStatusId(ms.getMissionStatusId())
                            .missionContent(ms.getMission().getMissionContent())
                            .getTime(ms.getGetTime())
                            .successTime(ms.getSuccessTime())
                            .confirmTime(ms.getConfirmTime());
                    break;
                }
            }
            missionResultResList.add(mrRes.build());
        }
//        for (MissionResultRes mrRes : missionResultResList) {
//            System.out.println(mrRes.toString());
//        }
        return missionResultResList;
    }
}