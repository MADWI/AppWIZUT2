# Uprawnienia
Dodałem kod odpowiedzialny za uzyskanie uprawnień do zapisu danych (Android 23+)

#Dane offline
Przykład wykorzystania nowego offline API 

         PlanyChangesHandler handler = new PlanyChangesHandler(getBaseContext());
         //zwraca najnowsza zmiane w planie
            handler.getLastMessage(new PlanyChangesHandler.DataCallback() {
                @Override
                public void foundData(ArrayList<MessagePlanChanges> data) {
                    System.out.println("ee");
                }
            });
            //zwraca wszystkie zmiany w planie
            handler.getCurrentData(new PlanyChangesHandler.DataCallback() {
                @Override
                public void foundData(ArrayList<MessagePlanChanges> data) {
                    System.out.println("ee");
                }
            });

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
            
            
