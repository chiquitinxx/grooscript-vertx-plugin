function DomainItem() {
  var gSobject = gs.inherit(gs.baseClass,'DomainItem');
  gSobject.clazz = { name: 'org.grooscript.domain.DomainItem', simpleName: 'DomainItem'};
  gSobject.clazz.superclass = { name: 'java.lang.Object', simpleName: 'Object'};
  gSobject.name = null;
  gSobject.date = null;
  gSobject.number = null;
  gSobject.__defineGetter__('constraints', function(){ return DomainItem.constraints; });
  gSobject.__defineSetter__('constraints', function(gSval){ DomainItem.constraints = gSval; });
  gSobject.__defineGetter__('mapping', function(){ return DomainItem.mapping; });
  gSobject.__defineSetter__('mapping', function(gSval){ DomainItem.mapping = gSval; });
  gSobject.id = null;
  gSobject.version = 0;
  gSobject.__defineGetter__('classNameWithoutPackage', function(){ return DomainItem.classNameWithoutPackage; });
  gSobject.__defineSetter__('classNameWithoutPackage', function(gSval){ DomainItem.classNameWithoutPackage = gSval; });
  gSobject['save'] = function(it) {
    var action = (gs.bool(gs.gp(gs.thisOrObject(this,gSobject),"id")) ? "update" : "create");
    var data = gs.mc(GrooscriptGrails,"getRemoteDomainClassProperties",gs.list([this]));
    return RemotePromise(gs.map().add("domainAction",action).add("className",gs.gp(gs.thisOrObject(this,gSobject),"classNameWithoutPackage")).add("data",data));
  }
  gSobject['delete'] = function(it) {
    return RemotePromise(gs.map().add("domainAction","delete").add("className",gs.gp(gs.thisOrObject(this,gSobject),"classNameWithoutPackage")).add("data",gs.map().add("id",gs.gp(gs.thisOrObject(this,gSobject),"id"))));
  }
  gSobject.get = function(x0) { return DomainItem.get(x0); }
  gSobject.list = function(x0) { return DomainItem.list(x0); }
  gSobject.DomainItem1 = function(map) { gs.passMapToObject(map,this); return this;};
  if (arguments.length==1) {gSobject.DomainItem1(arguments[0]); }
  
  return gSobject;
};
DomainItem.get = function(value) {
  return RemotePromise(gs.map().add("domainAction","read").add("className",gs.gp(gs.thisOrObject(this,gSobject),"classNameWithoutPackage")).add("data",gs.map().add("id",value)));
}
DomainItem.list = function(params) {
  return RemotePromise(gs.map().add("domainAction","list").add("className",gs.gp(gs.thisOrObject(this,gSobject),"classNameWithoutPackage")).add("data",gs.elvis(gs.bool(params) , params , gs.map())));
}
DomainItem.constraints = function(it) {
  gs.mc(gSobject,"name",gs.list([gs.map().add("nullable",false).add("maxSize",12)]));
  gs.mc(gSobject,"date",gs.list([gs.map().add("nullable",true)]));
  return gs.mc(gSobject,"number",gs.list([gs.map().add("nullable",true)]));
};
DomainItem.mapping = function(it) {
};
DomainItem.classNameWithoutPackage = "DomainItem";

