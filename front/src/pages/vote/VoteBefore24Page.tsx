import { useEffect, useState } from 'react';

import Button from '@/components/commons/Button';
import Card from '@/components/commons/Card';
import Loading from '@/components/commons/Loading';
import VoteRadioButtonList from '@/components/vote/VoteRadioButtonList';
import { partyState } from '@/recoil/atom';
import { partyListAll, votePersonService } from '@/services/voteService';
import styles from '@/styles/vote/VoteBefore24Page.module.css';
import { ListProfile } from '@/types/user.interface';
import { useRecoilValue } from 'recoil';
import Swal from 'sweetalert2';

function VoteBefore24Page({
  guestId,
  myProfile,
  setMyProfile,
}: {
  guestId: number;
  myProfile: ListProfile;
  setMyProfile: React.Dispatch<React.SetStateAction<ListProfile>>;
}) {
  const party = useRecoilValue(partyState);

  const [partyList, setPartyList] = useState<ListProfile[]>([]);
  // const [partyList, setPartyList] = useState<ListProfile[]>(PARTYLIST);
  const [loading, setLoading] = useState(true);
  const [selectedValue, setSelectedValue] = useState(0);

  useEffect(() => {
    partyListAll(party.partyId)
      .then((data) => {
        setPartyList(data);
        setLoading(false);
      })
      .catch((err) => {
        console.log(err);
      });
  }, []);
  // 라디오 버튼을 누르면 selectedValue값이 바뀜
  function voteRadioButtonListHandler(event: React.ChangeEvent<HTMLInputElement>) {
    console.log(parseInt(event.target.value));
    setSelectedValue(parseInt(event.target.value));
  }

  const voteFinish = () => {
    if (selectedValue === 0) {
      Swal.fire({
        icon: 'info',
        html: '나의 마니또에게 투표해주세요.',
        confirmButtonColor: '#eea23e',
      });
    } else {
      votePersonService(guestId, selectedValue);
      setMyProfile((prevMyProfileState) => ({
        ...prevMyProfileState,
        votedId: selectedValue,
      }));
    }
  };

  const children = (
    <div className={styles.Container}>
      <div className={`FontL FontBasic ${styles.Title}`}>내 마니또는 누구?</div>
      <div className={styles.ScrollConatiner}>
        {
          <VoteRadioButtonList
            partyList={partyList.filter((it) => it.guestId !== myProfile.guestId)}
            onChange={voteRadioButtonListHandler}
            value={selectedValue}
          />
        }
      </div>
      <Button onClickHandler={voteFinish} bgc={`${selectedValue ? 'voteFilled' : 'voteEmptyGray'}`}>
        투표하기
      </Button>
    </div>
  );
  return loading ? (
    <Loading />
  ) : (
    <div>
      <Card {...{ tag: 2, children: children }} />
    </div>
  );
}

export default VoteBefore24Page;
