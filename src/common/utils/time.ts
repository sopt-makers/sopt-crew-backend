export const todayDate = (): Date => {
  const date = new Date();
  date.setHours(date.getHours() - 3);
  return date;
};
