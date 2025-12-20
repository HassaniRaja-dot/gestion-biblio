package ma.est.meknes.bibliotheque.service;

import ma.est.meknes.bibliotheque.model.Emprunt;
import ma.est.meknes.bibliotheque.model.Adherent;
import ma.est.meknes.bibliotheque.model.Livre;
import ma.est.meknes.bibliotheque.dao.EmpruntDAO;
import ma.est.meknes.bibliotheque.dao.AdherentDAO;
import ma.est.meknes.bibliotheque.dao.LivreDAO;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;


public class EmpruntService {
    
    private EmpruntDAO empruntDAO;
    private AdherentDAO adherentDAO;
    private LivreDAO livreDAO;
    private JournalService journalService;
    private static final int DUREE_EMPRUNT_JOURS = 14;
    private static final int MAX_EMPRUNTS_ACTIFS = 3;
    private static final int DELAI_RETARD_JOURS = 10;

    public EmpruntService() {
        this.empruntDAO = new EmpruntDAO();
        this.adherentDAO = new AdherentDAO();
        this.livreDAO = new LivreDAO();
        this.journalService = new JournalService();
    }
    
    public boolean emprunterLivre(int adherentId, int livreId) {
        try {

            Adherent adherent = adherentDAO.findById(adherentId);
            if (adherent == null) {
                System.err.println("Adhérent non trouvé");
                return false;
            }
            

            Livre livre = livreDAO.findById(livreId);
            if (livre == null) {
                System.err.println("Livre non trouvé");
                return false;
            }
            

            if (adherent.isBloque()) {
                System.err.println("L'adhérent est bloqué");
                return false;
            }
            
            
            int nbEmpruntsActifs = empruntDAO.compterEmpruntsActifs(adherentId);
            if (nbEmpruntsActifs >= MAX_EMPRUNTS_ACTIFS) {
                System.err.println("Limite d'emprunts atteinte (3 max)");
                return false;
            }
            

            if (livre.getExempairesDisponibles() <= 0) {
                System.err.println("Aucun exemplaire disponible");
                return false;
            }
            
            Emprunt emprunt = new Emprunt();
            emprunt.setAdherent(adherent);
            emprunt.setLivre(livre);
            emprunt.setDateEmprunt(LocalDate.now());
            emprunt.setDateRetourPrevue(LocalDate.now().plusDays(DUREE_EMPRUNT_JOURS));
            emprunt.setStatut("ACTIF");
            
            empruntDAO.save(emprunt);
            

            livre.setExempairesDisponibles(livre.getExempairesDisponibles() - 1);
            livreDAO.update(livre);
            
            journalService.log("EMPRUNT_LIVRE", 
                "Emprunt : " + adherent.getNom() + " a emprunté " + livre.getTitre());
            
            return true;
        } catch (Exception e) {
            System.err.println("Erreur lors de l'emprunt : " + e.getMessage());
            return false;
        }
    }
    public boolean retournerLivre(int empruntId) {
        try {
            Emprunt emprunt = empruntDAO.findById(empruntId);
            if (emprunt == null) {
                System.err.println("Emprunt non trouvé");
                return false;
            }
            
            if (!"ACTIF".equals(emprunt.getStatut())) {
                System.err.println("Cet emprunt n'est pas actif");
                return false;
            }
            
    
            LocalDate dateRetour = LocalDate.now();
            emprunt.setDateRetourEffective(dateRetour);
            if (dateRetour.isAfter(emprunt.getDateRetourPrevue())) {
                long joursRetard = ChronoUnit.DAYS.between(
                    emprunt.getDateRetourPrevue(), 
                    dateRetour
                );
                emprunt.setStatut("RETARD");
                if (joursRetard > DELAI_RETARD_JOURS) {
                    Adherent adherent = emprunt.getAdherent();
                    adherent.setBloque(true);
                    adherentDAO.update(adherent);
                }
            } else {
                emprunt.setStatut("RENDU");
            }
            empruntDAO.update(emprunt);
            Livre livre = emprunt.getLivre();
            livre.setExempairesDisponibles(livre.getExempairesDisponibles() + 1);
            livreDAO.update(livre);
            journalService.log("RETOUR_LIVRE", 
                "Retour : " + emprunt.getAdherent().getNom() + " a retourné " + livre.getTitre());
            return true;
        } catch (Exception e) {
            System.err.println("Erreur lors du retour : " + e.getMessage());
            return false;
        }
    }
    public List<Emprunt> obtenirEmpruntsActifs(int adherentId) {
        return empruntDAO.findEmpruntsActifs(adherentId);
    }
    public List<Emprunt> obtenirHistoriqueEmprunts(int adherentId) {
        return empruntDAO.findByAdherent(adherentId);
    }
    public List<Emprunt> obtenirTousLesEmprunts() {
        return empruntDAO.findAll();
    }
    public Emprunt obtenirEmpruntParId(int id) {
        return empruntDAO.findById(id);
    }
}
