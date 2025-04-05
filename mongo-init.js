db = db.getSiblingDB("rappi");

db.createUser({
    user: "mongo",
    pwd: "123456",
    roles: [{ role: "readWrite", db: "rappi" }]
});