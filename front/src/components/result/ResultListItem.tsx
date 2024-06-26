import hat from '@/assets/images/hat.png';
import ResultListItemProfile from '@/components/result/ResultListItemProfile';
import styles from '@/styles/result/ResultListItem.module.css';
import { ResultListItemProps } from '@/types/result';
import { Heart } from '@phosphor-icons/react';

function ResultListItem(resultListItemProps: ResultListItemProps) {
  return (
    <div className={styles.PairFlex}>
      <ResultListItemProfile
        {...{ nickname: resultListItemProps.manito.nickname, thumbnailUrl: resultListItemProps.manito.thumbnailUrl }}
      />
      <div className={styles.HeartFlex}>
        <Heart size={32} color="#f26969" weight="fill" />
        <div className="FontXSBold FontBasic">
          친밀도 {resultListItemProps.maniti.favorability.manitoFavorability}점
        </div>
      </div>
      <div className={styles.ManitiContainer}>
        <img
          className={`${styles.Hat} 
        ${
          resultListItemProps.maniti.votedId === resultListItemProps.manito.guestId
            ? styles.HatActive
            : styles.HatHidden
        }`}
          src={hat}
          alt=""
        />
        <ResultListItemProfile
          {...{ nickname: resultListItemProps.maniti.nickname, thumbnailUrl: resultListItemProps.maniti.thumbnailUrl }}
        />
      </div>
    </div>
  );
}

export default ResultListItem;
