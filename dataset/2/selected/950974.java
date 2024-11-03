package sat;

import java.io.InputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.URL;
import padmig.DontMigrate;
import padmig.MigrationException;
import padmig.Migratory;
import padmig.Undock;
import pubweb.AbortedException;
import pubweb.bsp.BSPMigratable;
import pubweb.bsp.BSPMigratableProgram;

public class SatMig implements BSPMigratableProgram {

    @Migratory
    public void bspMain(BSPMigratable bsp, Serializable arg) throws AbortedException {
        if (!(arg instanceof String[])) {
            bsp.abort(new RuntimeException("String[] as arguments expected"));
        }
        @DontMigrate String[] args = (String[]) arg;
        long t1 = 0L, t2;
        try {
            if (bsp != null) bsp.printStdOut(bsp.getHostname()); else System.out.println(InetAddress.getLocalHost().getCanonicalHostName());
        } catch (Exception e) {
            if (bsp != null) bsp.printStdOut("exception: " + e); else System.out.println("exception: " + e);
        }
        FormulaMig formula = null;
        if (bsp == null || bsp.getProcessId() == 0) {
            try {
                @DontMigrate InputStream src;
                @DontMigrate String input;
                if (args.length > 0) input = args[0]; else input = "sat/uuf175-03.cnf";
                if (bsp != null) bsp.printStdOut("using formula definition: " + input); else System.out.println("using formula definition: " + input);
                if (bsp != null) {
                    bsp.printStdOut("trying to read formula from class file server of the job owner's peer");
                    src = bsp.getResourceAsStream(input);
                } else {
                    System.out.println("trying to read formula from http://www.upb.de/fachbereich/AG/agmadh/WWW/bono/" + input);
                    @DontMigrate URL url = new URL("http", "www.upb.de", -1, "/fachbereich/AG/agmadh/WWW/bono/" + input, null);
                    src = url.openStream();
                }
                formula = new FormulaMig(src, bsp);
            } catch (Exception e) {
                if (bsp != null) {
                    bsp.printStdOut("cannot load formula: " + e);
                    bsp.abort(e);
                } else {
                    System.out.println("cannot load formula: " + e);
                    System.exit(1);
                }
            }
            formula.print(bsp);
        }
        if (bsp != null) {
            bsp.syncMig();
            if (bsp.getProcessId() == 0) t1 = bsp.getTime();
            FormulaMig.parallelSolve(formula, bsp);
            bsp.syncMig();
            if (bsp.getProcessId() == 0) {
                t2 = bsp.getTime();
                bsp.printStdOut("consumed time: " + ((t2 - t1) / 1000) + "s");
            }
        } else formula.solve(bsp);
        if (bsp == null || bsp.getProcessId() == 0) {
            if (formula.isSatisfiable()) {
                if (bsp != null) bsp.printStdOut("satisfiable"); else System.out.println("satisfiable");
            } else {
                if (bsp != null) bsp.printStdOut("not satisfiable"); else System.out.println("not satisfiable");
            }
            formula.printVariables(bsp);
        }
    }

    @Undock
    public void migratoryWrapper(String[] args) throws AbortedException, MigrationException {
        bspMain(null, args);
    }

    public static void main(String[] args) throws AbortedException {
        try {
            SatMig sat = new SatMig();
            sat.migratoryWrapper(args);
        } catch (MigrationException e) {
            System.err.println("migration failed:");
            e.printStackTrace();
        }
    }
}
