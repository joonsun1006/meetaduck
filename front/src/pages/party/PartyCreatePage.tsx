import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';

import duckLogo from '@/assets/images/RubberDuckWithLogo.png';
import Button from '@/components/commons/Button';
import Input from '@/components/commons/Input';
import { loginState, partyState } from '@/recoil/atom';
import { Axios } from '@/services/axios';
import style from '@/styles/commons/Input.module.css';
import styles from '@/styles/party/Partyjoin.module.css';
import { useRecoilValue, useSetRecoilState } from 'recoil';

function PartyCreatePage() {
  const [partyName, setPartyName] = useState('');
  const [nameCount, setNameCount] = useState(0);
  const navigate = useNavigate();

  const login = useRecoilValue(loginState);
  const party = useRecoilValue(partyState);

  const setLogin = useSetRecoilState(loginState);
  const setParty = useSetRecoilState(partyState);
  const createHandler = async () => {
    try {
      const response = await Axios.post('/api/parties', {
        partyName: partyName,
        userId: login.userId,
      });

      setLogin((prevLoginState) => ({
        ...prevLoginState,
        partyId: response.data.partyId,
      }));
      setParty((prevPartyState) => ({
        ...prevPartyState,
        accessCode: response.data.accessCode,
        partyName: partyName,
        userId: login.userId,
        partyId: response.data.partyId,
      }));
      navigate('/partymaker');
    } catch (err) {
      console.log(err);
      alert(err);
    }
  };

  useEffect(() => {
    console.log('party.partyId :', party.partyId);
  }, [party]);

  const handleInputChange = (value: string) => {
    setPartyName(value);
  };

  return (
    <div className={styles.container}>
      <img src={duckLogo} alt="duckLogo" className={styles.marginTop} />
      <div className={`FontBasic FontL ${styles.joinTitle} `}>파티명</div>
      <div className={`${styles.marginTop}`}>
        <Input className={style.partyBox} maxLength={5} usersInput={partyName} onChange={handleInputChange} />
      </div>
      <div className={styles.marginTop}>
        <Button onClickHandler={createHandler} bgc="filled">
          파티열기
        </Button>
      </div>
      {/* <div className={styles.noPartySection}>
        <div>오늘의 명언(삭제예정)</div>
        <div>먼저핀꽃은 먼저진다</div>
        <div>남보다 먼저 공을 세우려고</div>
        <div>조급히 서둘것이 아니다</div>
        <div>– 채근담</div>
      </div> */}
    </div>
  );
}

export default PartyCreatePage;
