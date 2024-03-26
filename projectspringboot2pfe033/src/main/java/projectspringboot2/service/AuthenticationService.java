package projectspringboot2.service;

import projectspringboot2.model.AuthenticationResponse;
import projectspringboot2.model.Utilisateur;
import projectspringboot2.repository.UtilisateurRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final UtilisateurRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationService(UtilisateurRepository repository,
                                  PasswordEncoder passwordEncoder,
                                  JwtService jwtService,
                                  AuthenticationManager authenticationManager) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public AuthenticationResponse register(Utilisateur request) {
        // Vérifier si l'utilisateur existe déjà. Si oui, authentifier l'utilisateur
        if (repository.findByNomUtilisateur(request.getNomUtilisateur()).isPresent()) {
            return new AuthenticationResponse(null, "L'utilisateur existe déjà", null);
        }

        Utilisateur user = new Utilisateur();
        user.setPrenom(request.getPrenom());
        user.setNom(request.getNom());
        user.setNomUtilisateur(request.getNomUtilisateur());
        user.setMotDePasse(passwordEncoder.encode(request.getMotDePasse()));
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());

        user = repository.save(user);

        String jwt = jwtService.generateToken(user);

        return new AuthenticationResponse(jwt, "L'inscription de l'utilisateur a réussi", user.getRole());
    }

    public AuthenticationResponse authenticate(Utilisateur request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getNomUtilisateur(),
                        request.getMotDePasse()
                )
        );

        Utilisateur user = repository.findByNomUtilisateur(request.getNomUtilisateur()).orElseThrow();
        String jwt = jwtService.generateToken(user);

        // Retourner l'objet AuthenticationResponse avec le token et le rôle de l'utilisateur
        return new AuthenticationResponse(jwt, "La connexion de l'utilisateur a réussi", user.getRole());
    }
}
