package org.aksw.ocelot.common.db.mongo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.aksw.ocelot.common.config.CfgManager;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.bson.BsonArray;
import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.WriteResult;
import com.mongodb.util.JSON;

/**
 * Singleton.
 */
public class MongoManager {
  protected final static Logger LOG = LogManager.getLogger(MongoManager.class);

  public static XMLConfiguration config = CfgManager.getCfg(MongoManager.class);

  public static String HOST = "db.host";
  public static String PORT = "db.port";
  public static String NAME = "db.name";
  public static String COLLECTION = "db.collection";

  protected static MongoClient mc;
  protected DB db = null;
  public DBCollection coll = null;

  public String name = null;
  public String collection = null;

  public static final String idKey = "_id";

  public static MongoManager getMongoManager() {
    mc = new MongoClient(config.getString(HOST), config.getInt(PORT));
    final MongoManager mm = new MongoManager();
    return mm.getDefaultConfig();
  }

  public static MongoManager getMongoManager(final String host, final int port) {
    mc = new MongoClient(host, port);
    return new MongoManager();
  }

  /**
   * Singleton.
   */
  protected MongoManager() {
    //
  }

  public MongoManager getDefaultConfig() {
    name = config.getString(NAME);
    collection = config.getString(COLLECTION);
    return this;
  }

  public MongoManager setConfig(final String name, final String collection) {
    disconnect();
    this.name = name;
    return setCollection(collection);
  }

  public MongoManager setCollection(final String collection) {
    this.collection = collection;
    coll = null;
    return this;
  }

  public void disconnect() {
    db = null;
    coll = null;
  }

  @SuppressWarnings("deprecation")
  public void connect() {
    if (db == null) {
      db = mc.getDB(name);
    }
    if (coll == null) {
      try {
        coll = db.getCollection(collection);
      } catch (final Exception e) {
        LOG.error(e.getLocalizedMessage(), e);
        throw new RuntimeException(e);
      }
    }
  }

  public String insert(final String json) {
    try {
      final DBObject o = (DBObject) JSON.parse(json);
      if (o.get(idKey) == null) {
        final ObjectId id = ObjectId.get();
        o.put(idKey, id);
      }
      return insert(o);
    } catch (final Exception e) {
      LOG.error(e.getLocalizedMessage(), e);
      return "";
    }
  }

  private String insert(final DBObject o) {
    connect();
    try {
      coll.insert(o);
      return o.get(idKey).toString();
    } catch (final Exception e) {
      LOG.error(e.getLocalizedMessage(), e);
      return "";
    }
  }

  public boolean findDoc(final String json) {
    connect();
    return coll.find((DBObject) JSON.parse(json)).length() > 0 ? true : false;
  }

  public Iterator<DBObject> find(final DBObject dbObject) {
    connect();
    final DBCursor cursor = coll.find(dbObject);
    final Iterator<DBObject> iter = cursor.iterator();
    cursor.close();
    return iter;
  }

  public Iterator<DBObject> find(final String json) {
    return find((DBObject) JSON.parse(json));
  }

  public DBObject findDocumentById(final String id) {
    connect();
    final BasicDBObject obj = new BasicDBObject();
    obj.put(idKey, new ObjectId(id));
    return coll.findOne(obj);
  }

  public boolean deleteDocumentById(final DBObject o) {
    connect();
    final WriteResult wr = coll.remove(o);
    return wr.isUpdateOfExisting();
  }

  public boolean deleteDocumentById(final String id) {
    connect();
    final BasicDBObject obj = new BasicDBObject();
    obj.put(idKey, new ObjectId(id));
    return deleteDocumentById(obj);
  }

  public List<DBObject> getAll() {
    connect();
    final DBCursor cursorDoc = coll.find();
    final List<DBObject> list = new ArrayList<>();
    while (cursorDoc.hasNext()) {
      list.add((cursorDoc.next()));
    }
    cursorDoc.close();
    return list;
  }

  public void print() {
    connect();
    final DBCursor cursorDoc = coll.find();
    while (cursorDoc.hasNext()) {
      LOG.debug((cursorDoc.next()));
    }
    cursorDoc.close();
  }

  public Iterator<DBObject> findOperation(final String key, final String op, final BsonArray ba) {
    connect();
    return find(new Document(key, new Document(op, ba)).toJson());
  }

  public void deleteCollection() {
    connect();
    coll.drop();
  }

  public void deleteDB() {
    connect();
    db.dropDatabase();
  }
}
