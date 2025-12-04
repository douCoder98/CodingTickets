## 🚀 Comment lancer l'application

### Prérequis
- Java JDK 8+
- Apache Tomcat 9+

### Étapes de déploiement

1. **Importer le projet** dans votre IDE (Eclipse/IntelliJ)

2. **Configurer Tomcat** dans votre IDE et lancer le

3. **Déployer l'application**
   Clic droit sur le projet → `Run As > Run on Server`

4. **Accéder à l'application**
   ```
   http://localhost:8080/CodingTickets/login
   ```

---

## 🌐 URLs principales
|URL|Description|
|----------|----------|
|`/login`|Page de connexion|
|`/events`|Liste des événements disponibles|
|`/reservations/history`|Historique des réservations|
|`/logout`|Déconnexion|


---

## 👥 Comptes de test

### Clients

| Email | Mot de passe |
|-------|--------------|
| `jean.dupont@email.fr` | `password123` |
| `marie.martin@email.fr` | `password456` |
| `pierre.durand@email.fr` | `password789` |

### Organisateurs

| Email | Mot de passe |
|-------|--------------|
| `sophie.legrand@events.fr` | `org123` |
| `thomas.bernard@events.fr` | `org456` |

---

## 🎯 Utilisation rapide

1. Se connecter avec un compte client
2. Consulter les événements disponibles
3. Réserver des places
4. Consulter l'historique des réservations
5. Annuler une réservation (jusqu'à 24h avant l'événement)