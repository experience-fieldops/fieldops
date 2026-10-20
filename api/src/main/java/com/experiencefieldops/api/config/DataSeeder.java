package com.experiencefieldops.api.config;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.experiencefieldops.api.entities.Client;
import com.experiencefieldops.api.entities.Equipment;
import com.experiencefieldops.api.entities.Location;
import com.experiencefieldops.api.entities.User;
import com.experiencefieldops.api.entities.role.UserRole;
import com.experiencefieldops.api.repositories.ClientRepository;
import com.experiencefieldops.api.repositories.EquipmentRepository;
import com.experiencefieldops.api.repositories.LocationRepository;
import com.experiencefieldops.api.repositories.UserRepository;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepo;
    private final ClientRepository clientRepo;
    private final LocationRepository locationRepo;
    private final EquipmentRepository equipmentRepo;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepo, ClientRepository clientRepo,
            LocationRepository locationRepo, EquipmentRepository equipmentRepo,
            PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.clientRepo = clientRepo;
        this.locationRepo = locationRepo;
        this.equipmentRepo = equipmentRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        LocalDateTime now = LocalDateTime.now();

        // ── Usuários ──────────────────────────────────────────────
        if (userRepo.count() == 0) {
            userRepo.saveAll(List.of(
                new User(null, "Admin FieldOps",    "admin@fieldops.com",    passwordEncoder.encode("admin123"),    UserRole.ADMIN,    true, now, now),
                new User(null, "Carlos Mendes",     "gerente@fieldops.com",  passwordEncoder.encode("gerente123"),  UserRole.MANAGER,  true, now, now),
                new User(null, "João Silva",         "inspetor@fieldops.com", passwordEncoder.encode("inspetor123"), UserRole.INSPECTOR,true, now, now),
                new User(null, "Ana Costa",          "revisor@fieldops.com",  passwordEncoder.encode("revisor123"),  UserRole.REVIEWER, true, now, now),
                new User(null, "Mariana Ferreira",   "mferreira@fieldops.com",passwordEncoder.encode("admin123"),   UserRole.INSPECTOR,true, now, now),
                new User(null, "Pedro Almeida",      "palmeida@fieldops.com", passwordEncoder.encode("admin123"),   UserRole.INSPECTOR,true, now, now),
                new User(null, "Fernanda Lima",      "flima@fieldops.com",    passwordEncoder.encode("admin123"),   UserRole.MANAGER,  true, now, now),
                new User(null, "Ricardo Souza",      "rsouza@fieldops.com",   passwordEncoder.encode("admin123"),   UserRole.INSPECTOR,false, now, now)
            ));
            System.out.println("Usuarios criados: " + userRepo.count());
        }

        // ── Clientes ──────────────────────────────────────────────
        if (clientRepo.count() == 0) {
            List<Client> clients = clientRepo.saveAll(List.of(
                new Client(null, "Petrobras S.A.",          "operacoes@petrobras.com.br",  "(21) 3224-4477", "33.000.167/0001-01", true, now, now),
                new Client(null, "Vale S.A.",               "manutencao@vale.com",         "(31) 3299-6000", "33.592.510/0001-54", true, now, now),
                new Client(null, "Embraer S.A.",            "inspcao@embraer.com.br",      "(12) 3927-1000", "07.689.002/0001-89", true, now, now),
                new Client(null, "WEG S.A.",                "qualidade@weg.net",           "(47) 3276-4000", "84.429.695/0001-11", true, now, now),
                new Client(null, "Gerdau S.A.",             "manut@gerdau.com.br",         "(51) 3323-2000", "33.611.500/0001-19", true, now, now),
                new Client(null, "Braskem S.A.",            "insp@braskem.com.br",         "(11) 3576-9000", "42.150.391/0001-70", true, now, now),
                new Client(null, "Usiminas S.A.",           "oper@usiminas.com",           "(31) 3499-8000", "60.894.730/0001-05", true, now, now),
                new Client(null, "Rumo Logística",          "campo@rumo.com.br",           "(11) 3777-7000", "02.387.241/0001-60", true, now, now),
                new Client(null, "Eneva S.A.",              "eng@eneva.com.br",            "(21) 3721-3000", "04.423.567/0001-21", true, now, now),
                new Client(null, "Suzano S.A.",             "inspecao@suzano.com.br",      "(11) 3503-9000", "16.404.287/0001-55", true, now, now),
                new Client(null, "TechnipFMC Brasil",       "ops@technipfmc.com",          "(21) 2159-5000", "04.677.162/0001-27", true, now, now),
                new Client(null, "Saipem do Brasil",        "campo@saipem.com",            "(21) 2195-8000", "00.405.891/0001-77", true, now, now),
                new Client(null, "Construtora Queiroz Galvão", "qg@queirozgalvao.com.br",  "(11) 3030-2000", "33.412.792/0001-60", true, now, now),
                new Client(null, "Odebrecht Ambiental",    "amb@odebrecht.com.br",        "(71) 3311-8000", "21.255.847/0001-79", true, now, now),
                new Client(null, "Prysmian Cabos",          "prysmian@prysmian.com.br",    "(19) 3876-8000", "61.084.984/0001-60", false, now, now),
                new Client(null, "ABB Ltda.",               "abb@br.abb.com",              "(11) 5033-1000", "50.465.142/0001-70", true, now, now),
                new Client(null, "Siemens Brasil",          "siemens@siemens.com.br",      "(41) 3317-5000", "60.476.884/0001-04", true, now, now),
                new Client(null, "General Electric Brasil", "ge@ge.com",                   "(11) 2112-6000", "60.902.816/0001-02", true, now, now),
                new Client(null, "Schneider Electric",      "schneider@schneider.com.br",  "(19) 3754-7000", "51.477.016/0001-90", true, now, now),
                new Client(null, "Honeywell Brasil",        "info@honeywell.com.br",       "(11) 5185-1400", "43.874.059/0001-16", true, now, now),
                new Client(null, "Cemig Distribuição",      "manut@cemig.com.br",          "(31) 3506-5050", "06.981.180/0001-16", true, now, now),
                new Client(null, "Copel Geração",           "copel@copel.com",             "(41) 3331-4400", "45.168.597/0001-42", true, now, now),
                new Client(null, "Comgás S.A.",             "insp@comgas.com.br",          "(11) 3337-2700", "61.856.571/0001-17", true, now, now),
                new Client(null, "Transpetro S.A.",         "transpetro@transpetro.com.br","(21) 2157-1000", "04.621.926/0001-59", true, now, now)
            ));

            // ── Locais ────────────────────────────────────────────
            Client petro = clients.get(0);
            Client vale  = clients.get(1);
            Client embra = clients.get(2);
            Client weg   = clients.get(3);
            Client gerd  = clients.get(4);
            Client brask = clients.get(5);
            Client usim  = clients.get(6);
            Client rumo  = clients.get(7);
            Client eneva = clients.get(8);
            Client suzano= clients.get(9);

            List<Location> locs = locationRepo.saveAll(List.of(
                new Location(null, "Refinaria Duque de Caxias",   "Av. Governador Leonel Brizola, 2000", "Duque de Caxias", "RJ", "25221-000", petro, true, now, now),
                new Location(null, "Plataforma P-76",              "Bacia de Campos, km 0",               "Macaé",           "RJ", "27910-000", petro, true, now, now),
                new Location(null, "Terminal de Angra",            "Rua da Praia, 100",                   "Angra dos Reis",  "RJ", "23900-000", petro, true, now, now),
                new Location(null, "Mina de Carajás",              "Estrada de Ferro Carajás, km 125",    "Parauapebas",     "PA", "68515-000", vale,  true, now, now),
                new Location(null, "Pelotizadora de Tubarão",      "Av. Espírito Santo, 3000",            "Serra",           "ES", "29163-000", vale,  true, now, now),
                new Location(null, "Porto de Ponta da Madeira",    "Rodovia MA-206, km 5",                "São Luís",        "MA", "65093-220", vale,  true, now, now),
                new Location(null, "Fábrica de Aeronaves — Gavião Peixoto", "Rod. SP-333, km 196",       "Gavião Peixoto",  "SP", "14830-000", embra, true, now, now),
                new Location(null, "Centro de Ensaios em Voo",     "Av. Brigadeiro Faria Lima, 2170",     "São José dos Campos","SP","12227-901", embra, true, now, now),
                new Location(null, "Planta WEG Jaraguá do Sul",    "Av. Prefeito Waldemar Grubba, 3300",  "Jaraguá do Sul",  "SC", "89256-900", weg,   true, now, now),
                new Location(null, "Planta WEG Blumenau",          "Rua das Indústrias, 1000",            "Blumenau",        "SC", "89065-100", weg,   true, now, now),
                new Location(null, "Usina Siderúrgica Ouro Branco","Rodovia MG-443, km 3",               "Ouro Branco",     "MG", "36420-000", gerd,  true, now, now),
                new Location(null, "Complexo Siderúrgico Pindamonhangaba","Rod. Presidente Dutra, km 154","Pindamonhangaba","SP","12412-000", gerd,  true, now, now),
                new Location(null, "Polo Petroquímico de Camaçari","Av. Gal. Eurico G. Dutra, s/n",      "Camaçari",        "BA", "42810-000", brask, true, now, now),
                new Location(null, "Polo Petroquímico de Triunfo", "Rodovia BR-386, km 419",              "Triunfo",         "RS", "95870-000", brask, true, now, now),
                new Location(null, "Usiminas Ipatinga",            "Av. Pedro Linhares Gomes, 5852",      "Ipatinga",        "MG", "35160-900", usim,  true, now, now),
                new Location(null, "Terminal Ferroviário Rondonópolis","Av. Lions Internacional, 1000",   "Rondonópolis",    "MT", "78740-000", rumo,  true, now, now),
                new Location(null, "Usina Termelétrica Itaqui",    "Porto do Itaqui, s/n",                "São Luís",        "MA", "65095-000", eneva, true, now, now),
                new Location(null, "Fábrica Suzano Limeira",       "Rodovia Anhanguera, km 174",          "Limeira",         "SP", "13480-000", suzano,true, now, now),
                new Location(null, "Planta Suzano Imperatriz",     "Rod. MA-006, km 12",                  "Imperatriz",      "MA", "65915-000", suzano,true, now, now),
                new Location(null, "Fábrica ABB Guarulhos",        "Av. Anhanguera, km 21",               "Guarulhos",       "SP", "07276-000", clients.get(15), true, now, now),
                new Location(null, "Centro Siemens Curitiba",      "Av. Indianópolis, 3100",              "Curitiba",        "PR", "82230-001", clients.get(16), true, now, now),
                new Location(null, "Planta GE Campinas",           "Rod. Dom Pedro I, km 61",             "Campinas",        "SP", "13064-716", clients.get(17), true, now, now),
                new Location(null, "Schneider Electric Cajamar",   "Rod. Anhanguera, km 18",              "Cajamar",         "SP", "07751-000", clients.get(18), true, now, now),
                new Location(null, "Honeywell São Paulo",          "Av. Tamboré, 1180",                   "Barueri",         "SP", "06460-000", clients.get(19), true, now, now),
                new Location(null, "Subestação Cemig BH Norte",    "Av. Antônio Carlos, 8000",            "Belo Horizonte",  "MG", "31270-901", clients.get(20), true, now, now),
                new Location(null, "UHE Foz do Areia — Copel",    "Rodovia PR-469, km 15",               "Pinhão",          "PR", "85175-000", clients.get(21), true, now, now),
                new Location(null, "Estação Reguladora Comgás SP", "Av. do Estado, 5200",                 "São Paulo",       "SP", "03101-000", clients.get(22), true, now, now),
                new Location(null, "Terminal Transpetro Paulínia",  "Rod. SP-332, km 1",                  "Paulínia",        "SP", "13140-000", clients.get(23), true, now, now),
                new Location(null, "Refinaria REPLAN — Transpetro","Rod. SP-332, km 7",                   "Paulínia",        "SP", "13140-000", clients.get(23), true, now, now),
                new Location(null, "Base de Operações Macaé",      "Av. dos Campos, 1250",                "Macaé",           "RJ", "27930-070", petro, true, now, now),
                new Location(null, "Plataforma FPSO Cidade de Ilhabela", "Bacia de Santos, BM-S-11",     "Santos",          "SP", "11010-000", petro, true, now, now),
                new Location(null, "Terminal Porto de Santos",      "Av. Presidente Wilson, 165",          "Santos",          "SP", "11013-100", rumo,  true, now, now),
                new Location(null, "Armazém Logístico Campinas",    "Rod. Dom Pedro I, km 72",             "Campinas",        "SP", "13090-000", rumo,  true, now, now),
                new Location(null, "Usina Termelétrica Porto do Pecém","Rua da Usina, s/n",               "São Gonçalo do Amarante","CE","62658-000", eneva, true, now, now),
                new Location(null, "Mina de Ferro Brucutu",        "Estrada Brucutu, km 8",              "São Gonçalo do Rio Abaixo","MG","35748-000", vale, true, now, now),
                new Location(null, "Porto de Tubarão — Vale",      "Av. Hugo Musso, 1290",               "Vitória",         "ES", "29090-900", vale, true, now, now),
                new Location(null, "Planta Petroquímica ABC",      "Via Anchieta, km 36",                "São Bernardo do Campo","SP","09750-000", brask, true, now, now),
                new Location(null, "Centro de Distribuição Gerdau","Av. Industrial, 500",                "Contagem",        "MG", "32211-000", gerd,  true, now, now),
                new Location(null, "Fábrica Embraer Melbourne",    "32 Aviation Blvd",                   "Melbourne",       "FL", "00000-000", embra, false, now, now)
            ));

            // Equipamentos

            String[][] equips = {
                // {name, serial, category, status, locIndex}
                {"Bomba Centrífuga BC-101", "BC-101-2021", "Mecânico", "ATIVO", "0"},
                {"Compressor de Gás CG-201", "CG-201-2020", "Mecânico", "ATIVO", "0"},
                {"Transformador TR-001 138kV", "TR001-ABB-18", "Elétrico", "ATIVO", "0"},
                {"Válvula de Controle VC-301", "VC301-EMR-19", "Hidráulico", "EM_MANUTENCAO", "0"},
                {"Painel de Controle PLC-401", "PLC401-SIE-22", "Instrumentação", "ATIVO", "1"},
                {"Guindaste Offshore GD-501", "GD501-LIB-17", "Mecânico", "ATIVO", "1"},
                {"Sistema de Detecção de Gás SDG-601", "SDG601-HON-21", "Segurança", "ATIVO", "1"},
                {"Turbina a Gás TG-701", "TG701-GE-20", "Mecânico", "ATIVO", "1"},
                {"Motor Elétrico ME-801 75kW", "ME801-WEG-22", "Elétrico", "ATIVO", "2"},
                {"Caldeira Industrial CI-901", "CI901-BAB-18", "Mecânico", "ATIVO", "2"},
                {"Chiller HVAC CH-1001", "CH1001-CAR-20", "HVAC", "ATIVO", "2"},
                {"UPS Nobreak UPS-1101 20kVA", "UPS1101-APC-21", "Elétrico", "ATIVO", "3"},
                {"Britador de Mandíbulas BM-1201", "BM1201-MET-19", "Mecânico", "ATIVO", "3"},
                {"Correia Transportadora CT-1301", "CT1301-REX-20", "Mecânico", "EM_MANUTENCAO", "3"},
                {"Filtro Prensa FP-1401", "FP1401-LDS-18", "Mecânico", "ATIVO", "4"},
                {"Empilhadeira Elétrica EE-1501", "EE1501-TOY-22", "Mecânico", "ATIVO", "4"},
                {"Analisador de Qualidade AQ-1601", "AQ1601-ABB-21", "Instrumentação", "ATIVO", "4"},
                {"Ponte Rolante PR-1701 20t", "PR1701-DEM-17", "Estrutural", "ATIVO", "5"},
                {"Ventilador Industrial VI-1801", "VI1801-ZIE-19", "Mecânico", "ATIVO", "5"},
                {"Sistema de Supressão SS-1901", "SS1901-TYC-20", "Segurança", "ATIVO", "5"},
                {"Reator Químico RQ-2001", "RQ2001-PFD-18", "Mecânico", "ATIVO", "6"},
                {"Coluna de Destilação CD-2101", "CD2101-SPX-19", "Mecânico", "ATIVO", "6"},
                {"Compressor de Ar CA-2201", "CA2201-ATL-21", "Mecânico", "ATIVO", "6"},
                {"Trocador de Calor TC-2301", "TC2301-APV-20", "Mecânico", "ATIVO", "7"},
                {"Bomba de Transferência BT-2401", "BT2401-GRU-22", "Mecânico", "ATIVO", "7"},
                {"Quadro de Distribuição QD-2501", "QD2501-SCH-21", "Elétrico", "ATIVO", "7"},
                {"Gerador Diesel GD-2601 500kW", "GD2601-CAT-18", "Elétrico", "EM_MANUTENCAO", "8"},
                {"Sistema de Controle DCS-2701", "DCS2701-HON-20", "Instrumentação", "ATIVO", "8"},
                {"Bomba de Dosagem BD-2801", "BD2801-LAN-21", "Hidráulico", "ATIVO", "8"},
                {"Torre de Resfriamento TR-2901", "TR2901-BAL-19", "HVAC", "ATIVO", "9"},
                {"Motor de Indução MI-3001 200kW", "MI3001-WEG-22", "Elétrico", "ATIVO", "9"},
                {"Sensor de Pressão SP-3101", "SP3101-END-21", "Instrumentação", "ATIVO", "9"},
                {"Prensa Hidráulica PH-3201 200t", "PH3201-SCH-17", "Hidráulico", "ATIVO", "10"},
                {"Forno Industrial FI-3301", "FI3301-IND-18", "Mecânico", "ATIVO", "10"},
                {"Robô de Soldagem RS-3401", "RS3401-FAN-20", "Mecânico", "ATIVO", "10"},
                {"Transformador TR-3501 220kV", "TR3501-SIE-19", "Elétrico", "ATIVO", "11"},
                {"Disjuntor DJ-3601 138kV", "DJ3601-ABB-20", "Elétrico", "ATIVO", "11"},
                {"Medidor de Fluxo MF-3701", "MF3701-EMR-21", "Instrumentação", "ATIVO", "11"},
                {"Secador Rotativo SR-3801", "SR3801-BUH-18", "Mecânico", "ATIVO", "12"},
                {"Evaporador EV-3901", "EV3901-SPX-19", "Mecânico", "ATIVO", "12"},
                {"Bomba de Alta Pressão BAP-4001", "BAP4001-GRU-20", "Hidráulico", "ATIVO", "12"},
                {"Extrusor EX-4101", "EX4101-KRA-18", "Mecânico", "EM_MANUTENCAO", "13"},
                {"Misturador Industrial MI-4201", "MI4201-DIN-19", "Mecânico", "ATIVO", "13"},
                {"Caldeirão CA-4301", "CA4301-MES-21", "Mecânico", "ATIVO", "13"},
                {"Laminador LA-4401", "LA4401-SMS-17", "Mecânico", "ATIVO", "14"},
                {"Alto Forno AF-4501", "AF4501-PWT-16", "Estrutural", "ATIVO", "14"},
                {"Forno de Arco Elétrico FAE-4601", "FAE4601-DAD-18", "Elétrico", "ATIVO", "14"},
                {"Locomotiva Diésel LD-4701", "LD4701-EMD-15", "Mecânico", "ATIVO", "15"},
                {"Vagão Hopper VH-4801", "VH4801-VAL-18", "Estrutural", "ATIVO", "15"},
                {"Sistema de Sinalização SS-4901", "SS4901-SIE-19", "Elétrico", "ATIVO", "15"},
                {"Turbina a Vapor TV-5001 300MW", "TV5001-MHI-16", "Mecânico", "ATIVO", "16"},
                {"Gerador Síncrono GS-5101 300MVA", "GS5101-GE-16", "Elétrico", "ATIVO", "16"},
                {"Caldeira de Recuperação CR-5201", "CR5201-AND-17", "Mecânico", "EM_MANUTENCAO", "16"},
                {"Digestor DIG-5301", "DIG5301-MET-19", "Mecânico", "ATIVO", "17"},
                {"Polpeiro PLP-5401", "PLP5401-VAL-20", "Mecânico", "ATIVO", "17"},
                {"Evaporador de Múltiplo Efeito EME-5501", "EME5501-SPX-18", "Mecânico", "ATIVO", "18"},
                {"Caustificador CAU-5601", "CAU5601-AND-19", "Mecânico", "ATIVO", "18"},
                {"Transformador AT AT-5701 500kV", "AT5701-ABB-20", "Elétrico", "ATIVO", "19"}
            };

            for (String[] eq : equips) {
                int locIdx = Integer.parseInt(eq[4]);
                Location loc = locs.get(Math.min(locIdx, locs.size() - 1));
                equipmentRepo.save(new Equipment(null,
                    eq[0], eq[1],
                    "FO-" + eq[1].replaceAll("[^A-Z0-9]", "").substring(0, Math.min(8, eq[1].replaceAll("[^A-Z0-9]", "").length())),
                    eq[2], eq[3],
                    loc, now, now));
            }

            System.out.println("Seeder concluido: " + clients.size() + " clientes, " + locs.size() + " locais, " + equips.length + " equipamentos.");
        }
    }
}
