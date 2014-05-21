package org.intermine.bio.like;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.intermine.modelproduction.MetadataManager;
import org.intermine.modelproduction.MetadataManager.LargeObjectOutputStream;
import org.intermine.objectstore.ObjectStore;
import org.intermine.objectstore.intermine.ObjectStoreInterMineImpl;
import org.intermine.sql.Database;
import org.apache.log4j.Logger;

/**
 *
 * @author selma
 *
 */
public class Storing {

    private static final Logger LOG = Logger.getLogger(Storing.class);

    private Storing() {
        // Don't.
    }

    /**
     * writes index and associated directory to the database using the metadatamanager.
     *
     * @param os intermine objectstore
     * @param matrix one row of one precalculated matrix
     * @param aspectNumber save with this file name addition
     */
    public static void saveNormMatToDatabase(ObjectStore os,
            Map<Coordinates, Integer> matrix, String aspectNumber) {
        try {
            LOG.info("Deleting previous search index dirctory blob from db...");
            long startTime = System.currentTimeMillis();
            Database db = ((ObjectStoreInterMineImpl) os).getDatabase();
            boolean blobExisted = MetadataManager.deleteLargeBinary(db,
                    "LIKE_SIMILARITY_MATRIX" + aspectNumber);
            if (blobExisted) {
                LOG.debug("Deleting previous search index blob from db took: "
                        + (System.currentTimeMillis() - startTime) + ".");
            } else {
                LOG.debug("No previous search index blob found in db");
            }

            LOG.debug("Saving search index information to database...");
            writeObjectToDB(os, "LIKE_SIMILARITY_MATRIX" + aspectNumber, matrix);
            LOG.debug("Successfully saved search index information to database.");

        } catch (IOException e) {
            LOG.error(null, e);
            throw new RuntimeException("Index creation failed: ", e);
        } catch (SQLException e) {
            LOG.error(null, e);
            throw new RuntimeException("Index creation failed: ", e);
        }
    }

    public static void saveCommonMatToDatabase(ObjectStore os,
            Map<Coordinates, ArrayList<Integer>> matrix, String aspectNumber) {
        try {
            LOG.info("Deleting previous search index dirctory blob from db...");
            long startTime = System.currentTimeMillis();
            Database db = ((ObjectStoreInterMineImpl) os).getDatabase();
            boolean blobExisted = MetadataManager.deleteLargeBinary(db,
                    "LIKE_COMMON_MATRIX" + aspectNumber);
            if (blobExisted) {
                LOG.debug("Deleting previous search index blob from db took: "
                        + (System.currentTimeMillis() - startTime) + ".");
            } else {
                LOG.debug("No previous search index blob found in db");
            }

            LOG.debug("Saving search index information to database...");
            writeObjectToDB(os, "LIKE_COMMON_MATRIX" + aspectNumber, matrix);
            LOG.debug("Successfully saved search index information to database.");

        } catch (IOException e) {
            LOG.error(null, e);
            throw new RuntimeException("Index creation failed: ", e);
        } catch (SQLException e) {
            LOG.error(null, e);
            throw new RuntimeException("Index creation failed: ", e);
        }
    }

    private static void writeObjectToDB(ObjectStore os, String key, Object object)
            throws IOException, SQLException {
        LOG.debug("Saving stream to database...");
        Database db = ((ObjectStoreInterMineImpl) os).getDatabase();
        LargeObjectOutputStream streamOut = MetadataManager.storeLargeBinary(db, key);

        GZIPOutputStream gzipStream = new GZIPOutputStream(new BufferedOutputStream(streamOut));
        ObjectOutputStream objectStream = new ObjectOutputStream(gzipStream);

        LOG.debug("GZipping and serializing object...");
        objectStream.writeObject(object);

        objectStream.flush();
        gzipStream.finish();
        gzipStream.flush();

        streamOut.close();
    }

}