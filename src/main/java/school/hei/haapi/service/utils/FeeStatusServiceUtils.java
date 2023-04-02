    package school.hei.haapi.service.utils;

    import lombok.AllArgsConstructor;
    import lombok.Getter;
    import lombok.Setter;

    import java.time.LocalDate;

    @AllArgsConstructor
    @Setter
    @Getter
    public class FeeStatusServiceUtils {
            private String status;
            private double remainingAmount;

        public FeeStatusServiceUtils calculateNewRemainingAmount(double amountPaid) {
            int totalFeeAmount = 200000;
            double interestRate = 0.02;
            int daysLate = 0;
            int remainingAmount = 0;
            String status = "UNPAID";

            // Vérifier si la requête a été effectuée après le 15ème jour du mois
            LocalDate currentDate = LocalDate.now();
            int dayOfMonth = currentDate.getDayOfMonth();
            if (dayOfMonth > 15) {
                daysLate = dayOfMonth - 15;
                status = "LATE";

                // Calculer les intérêts composés et ajouter la pénalité de 2% à chaque jour de retard
                remainingAmount = totalFeeAmount;
                for (int i = 1; i <= daysLate; i++) {
                    double interest = remainingAmount * interestRate;
                    remainingAmount += interest;
                    double penalty = remainingAmount * 0.02;
                    remainingAmount += penalty;
                }
            } else {
                remainingAmount = totalFeeAmount;
            }

            // Vérifier si l'étudiant a déjà payé une partie des frais de scolarité
            if (amountPaid > 0 && amountPaid < totalFeeAmount) {
                double unpaidAmount = totalFeeAmount - amountPaid;
                if (dayOfMonth <= 15) {
                    remainingAmount += unpaidAmount;
                } else {
                    double interest = unpaidAmount * interestRate;
                    remainingAmount += unpaidAmount + interest;
                    double penalty = remainingAmount * 0.02;
                    remainingAmount += penalty;
                }
            } else if (amountPaid == totalFeeAmount) {
                remainingAmount = 0;
                if (dayOfMonth > 15) {
                    status = "LATE";
                } else {
                    status = "PAID";
                }
            }

            return new FeeStatusServiceUtils(status, remainingAmount);
        }



    }
