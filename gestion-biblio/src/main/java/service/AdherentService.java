package ma.est.meknes.bibliotheque.service;
import ma.est.meknes.bibliotheque.model.Adherent;
import ma.est.meknes.bibliotheque.dao.AdherentDAO;
import java.util.List;
public class AdherentService {
    private AdherentDAO adherentDAO;
    private JournalService journalService;
    private static final int MAX_EMPRUNTS_ACTIFS = 3;
    private static final int DELAI_RETARD_JOURS = 10;
    public AdherentService() {
        this.adherentDAO = new AdherentDAO();
        this.journalService = new JournalService();
    }
    public boolean ajouterAdherent(Adherent adherent) {
        try {
            if (adherentDAO.existeParEmail(adherent.getEmail())) {
                System.err.println("Email déjà enregistré");
                return false;
            }
            adherentDAO.save(adherent);
            journalService.log("AJOUT_ADHERENT", "Adhérent ajouté : " + adherent.getNom());
            return true;
        } catch (Exception e) {
            System.err.println("Erreur lors de l'ajout : " + e.getMessage());
            return false;
        }
    }
    public Adherent obtenirAdherentParId(int id) {
        return adherentDAO.findById(id);
    }
    

    public List<Adherent> obtenirTousLesAdherents() {
        return adherentDAO.findAll();
    }
    
    public boolean modifierAdherent(Adherent adherent) {
        try {
            adherentDAO.update(adherent);
            journalService.log("MODIFICATION_ADHERENT", "Adhérent modifié : " + adherent.getNom());
            return true;
        } catch (Exception e) {
            System.err.println("Erreur lors de la modification : " + e.getMessage());
            return false;
        }
    }
    public boolean supprimerAdherent(int id) {
        try {
            Adherent adherent = adherentDAO.findById(id);
            if (adherent == null) {
                return false;
            }       
            adherentDAO.delete(id);
            journalService.log("SUPPRESSION_ADHERENT", "Adhérent supprimé : " + adherent.getNom());
            return true;
        } catch (Exception e) {
            System.err.println("Erreur lors de la suppression : " + e.getMessage());
            return false;
        }
    }
    public boolean peutEmprunter(int id) {
        Adherent adherent = adherentDAO.findById(id);
        if (adherent == null) {
            return false;
        }
        if (adherent.isBloque()) {
            System.out.println("Adhérent bloqué en raison d'un retard");
            return false;
        }
        return true;
    }
    public void bloquerAdherent(int id) {
        Adherent adherent = adherentDAO.findById(id);
        if (adherent != null) {
            adherent.setBloque(true);
            adherentDAO.update(adherent);
            journalService.log("BLOCAGE_ADHERENT", "Adhérent bloqué : " + adherent.getNom());
        }
    }
    public void debloquerAdherent(int id) {
        Adherent adherent = adherentDAO.findById(id);
        if (adherent != null) {
            adherent.setBloque(false);
            adherentDAO.update(adherent);
            journalService.log("DEBLOQUAGE_ADHERENT", "Adhérent débloqué : " + adherent.getNom());
        }
    }
}
