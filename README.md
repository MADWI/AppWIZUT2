
#Dane offline
Przykład wykorzystania nowego offline API 


             WeekParityChecker checker = new WeekParityChecker(getBaseContext());
                  //zwraca dane parzystosci na dzien dzisiejszy i jutrzejszy
            checker.getNextDaysParity(new WeekParityChecker.DataTwoDaysCallback() {
                @Override
                public void twoDaysData(String[] data) {
                    System.out.println("ee");
                }
            });
            // zwraca cale posiadane dane parzystosci
            checker.getCurrentData(new WeekParityChecker.DataCallback() {
                @Override
                public void foundData(ArrayList<DayParity> data) {
                    System.out.println("ee");
                }
            });
            
            
