package org.xcom.mod.game;

public abstract class Armors {
	
	enum EAbility {
		eAbility_NONE,
		eAbility_Move,
		eAbility_Fly,
		eAbility_FlyUp,
		eAbility_FlyDown,
		eAbility_Launch,
		eAbility_Grapple,
		eAbility_ShotStandard,
		eAbility_RapidFire,
		eAbility_ShotStun,
		eAbility_ShotDroneHack,
		eAbility_ShotOverload,
		eAbility_ShotFlush,
		eAbility_ShotPaintTarget,
		eAbility_ShotPaintDefense,
		eAbility_MotionDetector,
		eAbility_ShotSniper_DEPRECATED,
		eAbility_ShotSuppress,
		eAbility_ShotSuppressII_DEPRECATED,
		eAbility_ShotSuppressArea_DEPRECATED,
		eAbility_ShotDamageCover,
		eAbility_TeslaHammer,
		eAbility_FragGrenade,
		eAbility_SmokeGrenade,
		eAbility_AlienGrenade,
		eAbility_RocketLauncher,
		eAbility_Aim,
		eAbility_Intimidate,
		eAbility_Mark,
		eAbility_HotPotato_DEPRECATED,
		eAbility_PFG,
		eAbility_PFGDropped,
		eAbility_PFGCookedOff,
		eAbility_Overwatch,
		eAbility_Torch,
		eAbility_Plague,
		eAbility_Stabilize,
		eAbility_Revive,
		eAbility_TakeCover,
		eAbility_Command,
		eAbility_Ghost,
		eAbility_MedikitHeal,
		eAbility_RepairSHIV,
		eAbility_CombatStim,
		eAbility_EquipWeapon,
		eAbility_Reload,
		eAbility_FlashBang_DEPRECATED,
		eAbility_MindMerge,
		eAbility_PsiLance,
		eAbility_PsiBoltII,
		eAbility_PsiBomb,
		eAbility_GreaterMindMerge,
		eAbility_PsiControl,
		eAbility_PsiPanic,
		eAbility_WarCry,
		eAbility_Berserk,
		eAbility_ReanimateAlly,
		eAbility_ReanimateEnemy,
		eAbility_PsiDrain,
		eAbility_PsiBless,
		eAbility_DoubleTap,
		eAbility_PrecisionShot,
		eAbility_DisablingShot,
		eAbility_FlareShot_DEPRECATED,
		eAbility_SquadSight,
		eAbility_TooCloseForComfort,
		eAbility_ShredderRocket,
		eAbility_ShotMayhem,
		eAbility_RunAndGun,
		eAbility_RifleSuppression_DEPRECATED,
		eAbility_BullRush,
		eAbility_BattleScanner,
		eAbility_Mindfray,
		eAbility_Rift,
		eAbility_TelekineticField,
		eAbility_MindControl,
		eAbility_PsiInspiration,
		eAbility_CloseCyberdisc,
		eAbility_DeathBlossom,
		eAbility_CannonFire,
		eAbility_ClusterBomb,
		eAbility_DestroyTerrain,
		eAbility_PsiInspired,
		eAbility_Repair,
		eAbility_ForceEndTurn_DEPRECATED,
		eAbility_HeatWave,
		eAbility_CivilianCover,
		eAbility_Bloodlust,
		eAbility_BloodCall,
		eAbility_MAX
	};
	
	enum EArmorProperty {
		eAP_None,
		eAP_Tank,
		eAP_Grapple,
		eAP_Psi,
		eAP_LightWeaponLimited,
		eAP_BackpackLimited,
		eAP_AirEvade,
		eAP_DualWield,
		eAP_PoisonImmunity,
		eAP_MAX
	};
	
	protected String name = "";
	
	protected EAbility[] ABILITIES = new EAbility[3];
	protected EArmorProperty[] Properties = new EArmorProperty[4];
	
	protected int iType = 0;;
	
	protected int iHPBonus = 0;
	protected int iDefenseBonus = 0;
	protected int iFlightFuel = 0;
	protected int iWillBonus = 0;
	protected int iLargeItems = 0;
	protected int iSmallItems = 0;
	protected int iMobilityBonus = 0;
	
	Armors(int iType, int iHPBonus, int iDefenseBonus, int iFlightFuel, int iWillBonus, int iLargeItems, int iSmallItems, int iMobilityBonus)
	{

		this.name = "";
		this.iType = iType;
		this.iHPBonus = iHPBonus;
		this.iDefenseBonus = iDefenseBonus;
		this.iFlightFuel = iFlightFuel;
		this.iWillBonus = iWillBonus;
		this.iLargeItems = iLargeItems;
		this.iSmallItems = iSmallItems;
		this.iMobilityBonus = iMobilityBonus;
		
	}
}
