import { LocalizationProvider } from '@mui/x-date-pickers';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';

import dayjs, { Dayjs } from 'dayjs';

import { partyState } from '@/recoil/atom';
import { useSetRecoilState, useRecoilValue } from 'recoil';
import { useEffect } from 'react';


function DatePickerInput({ setEndDate }) {
  // const [recommendForm, setRecommendForm] = useRecoilState(recommendFormState);


  const datePickerFormat = 'YYYY-MM-DD';

  // const datePickerUtils = {
  //   format: datePickerFormat,
  //   parse: (value) => dayjs(value, datePickerFormat, true).toDate(),
  // };

  // const endDateChange = (date) => {
  //   const formattedDate = dayjs(date).format(datePickerFormat);
  //   setRecommendForm((prev) => ({
  //     ...prev,
  //     endDate: formattedDate,
  //   }));
  // };

  return (
    <>
      <LocalizationProvider dateAdapter={AdapterDayjs}>
        <DatePicker
          label="종료 날짜를 선택해주세요"
          slotProps={{
            textField: {
              size: 'small',
            },
          }}
          format="YYYY / MM / DD"
          onChange={(newValue) => {
            setEndDate(newValue);
          }}
          // value={endDate}
          // onChange={(newValue) => {
          //   endDateChange(newValue);
          // }}
          shouldDisableDate={(day) => {
            return dayjs(dayjs(day as Dayjs).format(`YYYY-MM-DD`)).isBefore(`2024-03-27`);
          }}
        />
      </LocalizationProvider>
    </>
  );
}

export default DatePickerInput;
